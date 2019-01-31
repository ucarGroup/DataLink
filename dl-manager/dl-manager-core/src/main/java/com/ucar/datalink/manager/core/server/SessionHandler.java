package com.ucar.datalink.manager.core.server;

import com.google.common.collect.Lists;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.manager.core.coordinator.GroupCoordinator;
import com.ucar.datalink.manager.core.coordinator.ProtocolEntry;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.requests.*;
import org.apache.kafka.common.utils.Utils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SessionHandler：Manager和Worker进行Coordination的通信接口类，负责对不同类型的请求进行转发
 * 参考自[kafka.server.KafkaApis.scala]
 * 1.authorization相关的逻辑暂未实现
 * <p>
 * Created by lubiao on 2016/12/4.
 */
public class SessionHandler extends SimpleChannelHandler {
    private static final Logger logger = LoggerFactory.getLogger(SessionHandler.class);

    private GroupCoordinator coordinator;

    public SessionHandler(GroupCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    /**
     * Top-level method that handles all requests and multiplexes to the right api
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
        Request request = new Request(ctx, buffer.toByteBuffer());

        switch (ApiKeys.forId(request.getRequestId())) {
            case METADATA:
                handleMetadataRequest(ctx, request);
                break;
            case GROUP_COORDINATOR:
                handleGroupCoordinatorRequest(ctx, request);
                break;
            case JOIN_GROUP:
                handleJoinGroupRequest(ctx, request);
                break;
            case HEARTBEAT:
                handleHeartbeatRequest(ctx, request);
                break;
            case SYNC_GROUP:
                handleSyncGroupRequest(ctx, request);
                break;
            case LEAVE_GROUP:
                handleLeaveGroupRequest(ctx, request);
                break;
            default:
                throw new DatalinkException("Unknown api code " + request.getRequestId());
        }
    }

    private void handleMetadataRequest(ChannelHandlerContext ctx, Request request) {
        short requestVersion = request.getHeader().apiVersion();
        sendResponse(ctx,
                new Response(
                        new ResponseHeader(request.getHeader().correlationId()),
                        new MetadataResponse(
                                coordinator.getAllGroupCoordinators(),
                                "",
                                -1,
                                Lists.newArrayList(),
                                requestVersion
                        )
                )
        );
    }

    private void handleGroupCoordinatorRequest(ChannelHandlerContext ctx, Request request) {
        ResponseHeader responseHeader = new ResponseHeader(request.getHeader().correlationId());

        Node activeNode = coordinator.getActiveGroupCoordinator();
        if (activeNode == null) {
            logger.trace("Coordinator不存在");
            sendResponse(ctx,
                    new Response(responseHeader, new GroupCoordinatorResponse(Errors.GROUP_COORDINATOR_NOT_AVAILABLE.code(), Node.noNode())));
        } else {
            logger.trace("Coordinator存在,当前Coordinator是：{}", activeNode.toString());
            sendResponse(ctx,
                    new Response(responseHeader, new GroupCoordinatorResponse(Errors.NONE.code(), activeNode)));
        }
    }

    private void handleJoinGroupRequest(ChannelHandlerContext ctx, Request request) {
        JoinGroupRequest joinGroupRequest = (JoinGroupRequest) request.getBody();
        ResponseHeader responseHeader = new ResponseHeader(request.getHeader().correlationId());

        List<ProtocolEntry> protocols = joinGroupRequest.groupProtocols().stream().map(protocol -> new ProtocolEntry(protocol.name(), Utils.toArray(protocol.metadata()))).collect(Collectors.toList());
        coordinator.handleJoinGroup(
                joinGroupRequest.groupId(),
                joinGroupRequest.memberId(),
                request.getHeader().clientId(),
                request.getClientAddress().toString(),
                joinGroupRequest.rebalanceTimeout(),
                joinGroupRequest.sessionTimeout(),
                joinGroupRequest.protocolType(),
                protocols,
                (joinResult) -> {
                    Map<String, ByteBuffer> members = joinResult.getMembers().entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), k -> ByteBuffer.wrap(k.getValue())));
                    JoinGroupResponse responseBody = new JoinGroupResponse(request.getHeader().apiVersion(), joinResult.getErrorCode(), joinResult.getGenerationId(),
                            joinResult.getSubProtocol(), joinResult.getMemberId(), joinResult.getLeaderId(), members);

                    logger.trace(String.format("Sending join group response %s for correlation id %d to client %s.",
                            responseBody, request.getHeader().correlationId(), request.getHeader().clientId()));

                    sendResponse(ctx, new Response(responseHeader, responseBody));
                }
        );
    }

    private void handleHeartbeatRequest(ChannelHandlerContext ctx, Request request) {
        HeartbeatRequest heartbeatRequest = (HeartbeatRequest) request.getBody();
        ResponseHeader responseHeader = new ResponseHeader(request.getHeader().correlationId());

        coordinator.handleHeartbeat(
                heartbeatRequest.groupId(),
                heartbeatRequest.memberId(),
                heartbeatRequest.groupGenerationId(),
                (errorCode) -> {
                    HeartbeatResponse response = new HeartbeatResponse(errorCode);
                    logger.trace(String.format("Sending heartbeat response %s for correlation id %d to client %s.",
                            response, request.getHeader().correlationId(), request.getHeader().clientId()));
                    //当前manager不是active-manager，则结束和client的通信
                    if(errorCode == Errors.NOT_COORDINATOR_FOR_GROUP.code()){
                        ctx.getChannel().disconnect();
                    }else {
                        sendResponse(ctx, new Response(responseHeader, response));
                    }
                });
    }

    private void handleSyncGroupRequest(ChannelHandlerContext ctx, Request request) {
        SyncGroupRequest syncGroupRequest = (SyncGroupRequest) request.getBody();
        coordinator.handleSyncGroup(
                syncGroupRequest.groupId(),
                syncGroupRequest.generationId(),
                syncGroupRequest.memberId(),
                syncGroupRequest.groupAssignment().entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), k -> Utils.toArray(k.getValue()))),
                (memberState, errorCode) -> {
                    SyncGroupResponse responseBody = new SyncGroupResponse(errorCode, ByteBuffer.wrap(memberState));
                    ResponseHeader responseHeader = new ResponseHeader(request.getHeader().correlationId());
                    sendResponse(ctx, new Response(responseHeader, responseBody));
                }
        );
    }

    private void handleLeaveGroupRequest(ChannelHandlerContext ctx, Request request) {
        LeaveGroupRequest leaveGroupRequest = (LeaveGroupRequest) request.getBody();
        ResponseHeader responseHeader = new ResponseHeader(request.getHeader().correlationId());

        // let the coordinator to handle leave-group
        coordinator.handleLeaveGroup(
                leaveGroupRequest.groupId(),
                leaveGroupRequest.memberId(),
                (errorCode) -> {
                    LeaveGroupResponse response = new LeaveGroupResponse(errorCode);
                    sendResponse(ctx, new Response(responseHeader, response));
                });
    }

    private void sendResponse(ChannelHandlerContext ctx, Response response) {
        Channels.write(ctx.getChannel(), ChannelBuffers.wrappedBuffer(response.getBuffers()));
    }
}

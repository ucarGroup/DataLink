package com.ucar.datalink.manager.core.server;

import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.Protocol;
import org.apache.kafka.common.requests.AbstractRequest;
import org.apache.kafka.common.requests.ApiVersionsRequest;
import org.apache.kafka.common.requests.RequestHeader;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by lubiao on 2016/12/5.
 */
class Request {
    private short requestId;
    private RequestHeader header;
    private AbstractRequest body;
    private InetAddress clientAddress;

    Request(ChannelHandlerContext ctx, ByteBuffer buffer) {
        this.requestId = buffer.getShort();

        buffer.rewind();
        header = RequestHeader.parse(buffer);

        if (header.apiKey() == ApiKeys.API_VERSIONS.id && !Protocol.apiVersionSupported(header.apiKey(), header.apiVersion())) {
            body = new ApiVersionsRequest();
        } else {
            body = AbstractRequest.getRequest(header.apiKey(), header.apiVersion(), buffer);
        }

        this.clientAddress = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress();
    }

    short getRequestId() {
        return requestId;
    }

    RequestHeader getHeader() {
        return header;
    }

    AbstractRequest getBody() {
        return body;
    }

    InetAddress getClientAddress() {
        return clientAddress;
    }
}

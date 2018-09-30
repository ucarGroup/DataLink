package com.ucar.datalink.manager.core.server;

import com.ucar.datalink.manager.core.coordinator.GroupCoordinator;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.jboss.netty.handler.codec.replay.VoidEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by lubiao on 2016/12/4.
 *
 * Tcp-Server For (Re-)Balance,该Server主要用来和Worker进行Coordination,其它方式的交互用的是JettyServer(Http协议).
 */
public class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private String hostname;
    private int port;
    private Channel serverChannel = null;
    private ServerBootstrap bootstrap = null;
    private GroupCoordinator coordinator = null;

    public NettyServer(ManagerConfig config, GroupCoordinator coordinator) {
        this.hostname = config.getHostName();
        this.port = config.getPort();
        this.coordinator = coordinator;
    }

    public void startup() {
        this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        // 构造对应的pipeline
        bootstrap.setPipelineFactory(() -> {
            ChannelPipeline pipelines = Channels.pipeline();
            pipelines.addLast(FixedHeaderFrameDecoder.class.getName(), new FixedHeaderFrameDecoder());
            SessionHandler sessionHandler = new SessionHandler(coordinator);
            pipelines.addLast(SessionHandler.class.getName(), sessionHandler);
            return pipelines;
        });

        // 启动
        if (StringUtils.isNotEmpty(hostname)) {
            this.serverChannel = bootstrap.bind(new InetSocketAddress(this.hostname, this.port));
        } else {
            this.serverChannel = bootstrap.bind(new InetSocketAddress(this.port));
        }

        logger.info(" ##Netty Server is started.");
    }

    public void shutdown() {
        if (this.serverChannel != null) {
            this.serverChannel.close().awaitUninterruptibly(1000);
        }

        if (this.bootstrap != null) {
            this.bootstrap.releaseExternalResources();
        }

        logger.info(" ##Netty Server is shutdown.");
    }

    static class FixedHeaderFrameDecoder extends ReplayingDecoder<VoidEnum> {

        protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, VoidEnum state)
                throws Exception {
            return buffer.readBytes(buffer.readInt());
        }
    }
}

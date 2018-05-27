package com.consistenthash.framework;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private RpcSendHandler rpcSendHandler = new RpcSendHandler();
    private Channel channel;
    private EventLoopGroup group;

    private String ip;
    private int port;

    public RpcClient(String ipWithPort) {
        if (ipWithPort.contains(":")) {
            this.ip = ipWithPort.split(":")[0];
            this.port = Integer.valueOf(ipWithPort.split(":")[1]);
        } else {
            log.error("Can not find usable ip and port");
        }
    }

    public RpcClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public RpcSendHandler getRpcSendHandler() {
        return rpcSendHandler;
    }

    public void connect() {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                        pipeline.addLast("encoder", new ObjectEncoder());
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast("handler", rpcSendHandler);
                    }
                });
        channel = bootstrap.connect(ip, port).syncUninterruptibly().channel();
    }

    public void sendMessageRequest(MessageRequest messageRequest){

    }

    public void close(){
        group.shutdownGracefully();
        group=null;
    }
}

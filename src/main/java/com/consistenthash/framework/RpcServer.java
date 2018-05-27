package com.consistenthash.framework;

import com.consistenthash.Test;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;


public class RpcServer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String ip;
    private int port;

    private ServerBootstrap serverBootstrap;
    private ChannelFuture channelFuture;
    private ConcurrentHashMap<String, Object> classMap = null;
    private EventLoopGroup masterGroup;
    private EventLoopGroup workerGroup;

    public RpcServer(String ipWithPort) {
        if (ipWithPort.contains(":")) {
            this.ip = ipWithPort.split(":")[0];
            this.port = Integer.valueOf(ipWithPort.split(":")[1]);
        } else {
            log.error("Can not find usable ip and port");
        }
        classMap = new ConcurrentHashMap<String, Object>();
    }

    public RpcServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        classMap = new ConcurrentHashMap<String, Object>();
    }

    public void start() {
        masterGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(masterGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        pipeline.addLast(new LengthFieldPrepender(4));
                        pipeline.addLast("encoder", new ObjectEncoder());
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast(new RpcRecvHandler(classMap));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        channelFuture = serverBootstrap.bind(ip, port).syncUninterruptibly();
        log.info("Rpc Server is starting. IP:" + ip + " Port:" + port);

    }

    public void shutdown() {
        if(workerGroup!=null)
            workerGroup.shutdownGracefully();
        if(masterGroup!=null)
            masterGroup.shutdownGracefully();
        channelFuture.channel().closeFuture().syncUninterruptibly();
        log.info("Rpc Server has stopped.");
    }

    public void waitCore(){
        try {
            channelFuture.sync();
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void putClass(String name, Object cls) {
        this.classMap.put(name, cls);
    }
}

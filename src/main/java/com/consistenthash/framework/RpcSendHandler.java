package com.consistenthash.framework;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class RpcSendHandler extends ChannelInboundHandlerAdapter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private ConcurrentHashMap<String, MessageCallBack> mapCallBack = new ConcurrentHashMap<String, MessageCallBack>();
    private volatile Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Connect to RpcServer:"+ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof MessageResponse){
            MessageResponse response=(MessageResponse) msg;
            log.debug("Remote call response receive:"+response.toString());
            String messageId = response.getMessageId();
            MessageCallBack callBack = mapCallBack.get(messageId);
            if (callBack != null) {
                mapCallBack.remove(messageId);
                callBack.over(response);
            }
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getLocalizedMessage());
        ctx.close();
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public MessageCallBack sendRequest(final MessageRequest request) {
        MessageCallBack callBack = new MessageCallBack(request);
        mapCallBack.put(request.getMessageId(), callBack);
        channel.writeAndFlush(request);
        log.debug("Send RpcRequest to Server:"+request.toString());
        return callBack;
    }
}

package com.consistenthash.framework;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class RpcRecvHandler extends ChannelInboundHandlerAdapter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ConcurrentHashMap<String,Object> classMap;

    public RpcRecvHandler(ConcurrentHashMap<String,Object> classMap){
        assert(classMap!=null);
        this.classMap=classMap;
    }

    /**
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)  {
        log.debug("Receive rpc request:"+msg.toString());
        if(msg instanceof MessageRequest) {
            final MessageRequest messageRequest = (MessageRequest) msg;
            final MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessageId(messageRequest.getMessageId());

            String className = messageRequest.getClassName();
            String methodName = messageRequest.getMethodName();
            Class<?>[] methodType = messageRequest.getTypeParameters();
            Object serverClass = null;
            if(!this.classMap.containsKey(className)){
                try {
                    serverClass = Class.forName(className).newInstance();
                    classMap.put(className,serverClass);
                    log.warn("Auto create instance:"+className);
                }catch (Exception ex){
                    log.error(ex.getLocalizedMessage());
                    messageResponse.setError(ex.getLocalizedMessage());
                }
            }
            serverClass = classMap.get(className);
            if(null!=serverClass) {
                try {
                    Method method = serverClass.getClass().getMethod(methodName, methodType);
                    Object result = method.invoke(serverClass, messageRequest.getParametersVal());
                    messageResponse.setResult(result);
                }
                catch (Exception ex){
                    messageResponse.setResult(ex.getLocalizedMessage());
                }
            }
            ctx.writeAndFlush(messageResponse).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.debug("RPC Server send respone:" + messageResponse.toString());
                }
            });

        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connect from:"+ctx.channel().remoteAddress());
        super.channelRegistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getLocalizedMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnect from:"+ctx.channel().remoteAddress());
        super.channelUnregistered(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}

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
        this.classMap=classMap;
    }

    /**
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("receive");
        if(msg instanceof MessageRequest) {
            final MessageRequest messageRequest = (MessageRequest) msg;
            MessageResponse messageResponse = new MessageResponse();
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
                Method method = serverClass.getClass().getMethod(methodName, methodType);
                Object result = method.invoke(serverClass, messageRequest.getParametersVal());

                messageResponse.setResult(result);
            }
            ctx.writeAndFlush(messageResponse).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.error("RPC Server Send message-id respone:" + messageRequest.getMessageId());
                }
            });

        }
    }
}

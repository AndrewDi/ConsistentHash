package com.consistenthash.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class MessageSendProxy<T> implements InvocationHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Class<T> cls;
    private RpcClient rpcClient;

    public MessageSendProxy(Class<T> cls,RpcClient rpcClient) {
        this.cls = cls;
        this.rpcClient=rpcClient;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MessageRequest request = new MessageRequest();
        request.setMessageId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setTypeParameters(method.getParameterTypes());
        request.setParametersVal(args);

        log.debug("Start to remote call method:"+request.toString());
        RpcSendHandler handler = rpcClient.getRpcSendHandler();
        MessageCallBack callBack = handler.sendRequest(request);
        return callBack.start();
    }
}

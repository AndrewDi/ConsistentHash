package com.consistenthash.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class MessageSendProxy<T> implements InvocationHandler {

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

        RpcSendHandler handler = rpcClient.getRpcSendHandler();
        MessageCallBack callBack = handler.sendRequest(request);
        return callBack.start();
    }
}

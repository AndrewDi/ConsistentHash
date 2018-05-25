package com.consistenthash.framework;

import java.lang.reflect.Proxy;

public class RpcProxy {

    public static <T> T create(Class<T> rpcInterface,RpcClient rpcClient){
        return (T) Proxy.newProxyInstance(
                rpcInterface.getClassLoader(),
                new Class<?>[]{rpcInterface},
                new MessageSendProxy<T>(rpcInterface,rpcClient)
        );

    }
}

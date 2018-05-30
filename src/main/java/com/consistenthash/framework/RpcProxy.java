package com.consistenthash.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

public class RpcProxy {

    private static final Logger log = LoggerFactory.getLogger(RpcProxy.class);

    public static <T> T create(Class<T> rpcInterface,RpcClient rpcClient){
        if(!rpcClient.getStatus()) {
            log.error("Rpc client connect error");
            return null;
        }
        return (T) Proxy.newProxyInstance(
                rpcInterface.getClassLoader(),
                new Class<?>[]{rpcInterface},
                new MessageSendProxy<T>(rpcInterface,rpcClient)
        );

    }
}

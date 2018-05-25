package com.consistenthash;

import com.consistenthash.framework.RpcClient;
import com.consistenthash.framework.RpcProxy;

public class RpcClientMain {
    public static void main(String[] args) throws Exception {
        final RpcClient rpcClient = new RpcClient("127.0.0.1",12345);
        Runnable runnable = new Runnable() {
            public void run() {
                rpcClient.connect();
            }
        };
        runnable.run();
        ITest test = RpcProxy.create(ITest.class,rpcClient);
        test.add();
    }
}
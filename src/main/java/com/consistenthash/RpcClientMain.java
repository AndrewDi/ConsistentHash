package com.consistenthash;

import com.consistenthash.framework.RpcClient;
import com.consistenthash.framework.RpcProxy;
import com.sun.glass.ui.SystemClipboard;

public class RpcClientMain {
    public static void main(String[] args) throws Exception {
        RpcClient rpcClient = new RpcClient("127.0.0.1",12345);
        rpcClient.connect();
        ITest test = RpcProxy.create(ITest.class,rpcClient);
        for(int i=0;i<10;i++) {
            System.out.println(test.add());
        }
    }
}
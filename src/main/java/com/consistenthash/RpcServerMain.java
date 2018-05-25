package com.consistenthash;

import com.consistenthash.framework.RpcClient;
import com.consistenthash.framework.RpcServer;

public class RpcServerMain {

    public static void main(String[] args) throws Exception {
        RpcServer rpcServer = new RpcServer("127.0.0.1",12345);
        ITest test=new Test();
        rpcServer.putClass(ITest.class.getName(),test);
        rpcServer.start();
    }
}
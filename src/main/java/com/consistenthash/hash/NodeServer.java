package com.consistenthash.hash;

import com.consistenthash.framework.RpcClient;
import com.consistenthash.framework.RpcServer;

import java.util.LinkedList;
import java.util.List;

public class NodeServer {
    private Node physicalNode;
    private List<Node> virtualnodes;

    public NodeServer(String ip, int port, int vNodes){
        physicalNode = new Node(ip,port,0);
        virtualnodes = new LinkedList<>();
        for(int i=0;i<vNodes;i++){
            Node vNode = new Node(ip,port,i);
            virtualnodes.add(vNode);
        }
    }

    public void initNode(){
        RpcServer rpcServer = new RpcServer(physicalNode.getIp(),physicalNode.getPort());
        rpcServer.start();
        physicalNode.setRpcServer(rpcServer);
        for(Node node:virtualnodes){
            node.setRpcServer(rpcServer);
        }
    }

    public void initClientConnect(){
        Node[] nodes = physicalNode.getNodes().values().toArray(new Node[physicalNode.getNodes().size()]);
        for(Node node:nodes){
            //检查需要连接的节点是否是本机上的物理节点/虚拟节点
            if(node!=physicalNode&&(!node.getIp().endsWith(physicalNode.getIp())&&node.getPort()!=physicalNode.getPort())){
                RpcClient rpcClient = new RpcClient(node.getIp(),node.getPort());
                rpcClient.connect();
                physicalNode.getRpcClients().put(node.hashCode(),rpcClient);
            }
        }
    }
}

package com.consistenthash.hash;

import com.consistenthash.framework.RpcClient;
import com.consistenthash.framework.RpcServer;

import java.io.Serializable;
import java.util.Objects;
import java.util.TreeMap;

public class Node implements Serializable {
    private String ip;
    private int port;
    private RpcServer rpcServer;
    private int vNodeNum;
    private Node previous;
    private Node next;
    private TreeMap<Integer,Node> nodes;
    private TreeMap<Integer,RpcClient> rpcClients;

    public Node(String ip, int port, int vNodeNum) {
        this.ip = ip;
        this.port = port;
        this.vNodeNum = vNodeNum;
        this.nodes = new TreeMap<>();
        this.rpcClients = new TreeMap<>();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public RpcServer getRpcServer() {
        return rpcServer;
    }

    public void setRpcServer(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    public int getvNodeNum() {
        return vNodeNum;
    }

    public void setvNodeNum(int vNodeNum) {
        this.vNodeNum = vNodeNum;
    }

    public Node getPrevious() {
        if(previous ==null)
            return this;
        return previous;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    public Node getNext() {
        if(next==null)
            return this;
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public TreeMap<Integer, Node> getNodes() {
        return nodes;
    }

    public void setNodes(TreeMap<Integer, Node> nodes) {
        this.nodes = nodes;
    }

    public TreeMap<Integer, RpcClient> getRpcClients() {
        return rpcClients;
    }

    public void setRpcClients(TreeMap<Integer, RpcClient> rpcClients) {
        this.rpcClients = rpcClients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return port == node.port &&
                vNodeNum == node.vNodeNum &&
                Objects.equals(ip, node.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, vNodeNum);
    }
}

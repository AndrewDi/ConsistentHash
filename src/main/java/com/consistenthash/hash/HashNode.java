package com.consistenthash.hash;

import java.io.Serializable;

public class HashNode implements Serializable {
    private String ip;
    private int port;
    private NodeState nodeState=NodeState.Init;
    private boolean isVirtual=false;
    private int vNumber;
    private long startTime;

    public HashNode(String ip, int port, int vNumber) {
        this.ip = ip;
        this.port = port;
        this.vNumber = vNumber;
        this.startTime = System.currentTimeMillis();
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

    public NodeState getNodeState() {
        return nodeState;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean virtual) {
        isVirtual = virtual;
    }

    public int getvNumber() {
        return vNumber;
    }

    public void setvNumber(int vNumber) {
        this.vNumber = vNumber;
    }

    public void refreshStartTime(){
        this.startTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isEarly(long otherNodeStartTime){
        return this.startTime<otherNodeStartTime;
    }

    @Override
    public String toString() {
        return this.ip+this.port+this.vNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof HashNode))
            return false;
        HashNode hn = (HashNode)obj;
        return hn.ip.equals(this.ip)&&hn.port==this.port;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        HashNode hn = new HashNode(this.ip,this.port,this.vNumber);
        hn.nodeState = NodeState.Init;
        return hn;
    }
}
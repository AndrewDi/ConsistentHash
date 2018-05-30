package com.consistenthash.hash;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class HashNodeComm implements IHashNodeComm {
    private HashCircle hashCircle;
    private HashNode currentNode;

    public HashNodeComm(HashCircle _hashCircle){
        this.hashCircle = _hashCircle;
        this.currentNode = _hashCircle.getCurrentNode();
    }

    public HashNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(HashNode currentNode) {
        this.currentNode = currentNode;
    }

    public ConcurrentHashMap<Long,HashNode> syncNodeList(){
        return hashCircle.getNodeList();
    }

    public void join(HashNode node){
        this.hashCircle.getNodeList().putIfAbsent(HashUtils.hash(node.toString()),node);
    }

    public Boolean isAlive(){
        return true;
    }
}

package com.consistenthash.hash;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public interface IHashNodeComm {
    public HashNode getCurrentNode();
    public void setCurrentNode(HashNode currentNode);
    public ConcurrentHashMap<Long,HashNode> syncNodeList();
    public void join(HashNode node);
    public Boolean isAlive();
}

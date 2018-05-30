package com.consistenthash;

import com.consistenthash.hash.HashCircle;
import com.consistenthash.hash.HashNode;

import java.util.LinkedList;
import java.util.List;

public class HashMain {
    public static void main(String[] args) throws InterruptedException {
        List<HashNode> nodes = new LinkedList<HashNode>(){{
            add(new HashNode("127.0.0.1", 12345, 0));
            add(new HashNode("127.0.0.1", 12346, 0));
            add(new HashNode("127.0.0.1", 12347, 0));
        }};
        HashCircle hashCircle = HashCircle.createWithNode(nodes.get(0),nodes);
        hashCircle.start();
        Thread.sleep(1000);
        nodes.get(1).refreshStartTime();
        HashCircle hashCircle2 = HashCircle.createWithNode(nodes.get(1),nodes);
        hashCircle2.start();
        Thread.sleep(1000);
        nodes.get(2).refreshStartTime();
        HashCircle hashCircle3 = HashCircle.createWithNode(nodes.get(2),nodes);
        hashCircle3.start();

    }
}

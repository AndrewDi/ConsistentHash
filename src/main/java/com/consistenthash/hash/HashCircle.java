package com.consistenthash.hash;

import com.consistenthash.framework.RpcClient;
import com.consistenthash.framework.RpcProxy;
import com.consistenthash.framework.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class HashCircle {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ConcurrentHashMap<Long,IHashNodeComm> nodeComms;
    private ConcurrentHashMap<Long,RpcClient> rpcClients;
    private ConcurrentHashMap<Long,HashNode> nodeList;
    private HashNode masterNode;
    private HashNode currentNode;
    private RpcServer rpcServer;
    private IHashNodeComm nodeComm;

    public static HashCircle createWithNode(HashNode _node, List<HashNode> _nodelist, int _vNumbers){
        HashCircle hashCircle = new HashCircle();
        hashCircle.nodeComms = new ConcurrentHashMap<>();
        hashCircle.rpcClients = new ConcurrentHashMap<>();
        hashCircle.currentNode = _node;
        hashCircle.rpcServer = new RpcServer(_node.getIp(),_node.getPort());
        hashCircle.nodeComm = new HashNodeComm(hashCircle);

        //实例化接口实例，并将其放入RpcServer
        hashCircle.rpcServer.putClass(IHashNodeComm.class.getName(),hashCircle.nodeComm);
        hashCircle.rpcServer.start();
        //设置自己为主控制节点
        hashCircle.setMasterNode(_node);
        hashCircle.genTreeMap(_nodelist);

        //检查是否有已有的环存在
        for(HashNode hashNode:_nodelist){
            if(!hashNode.equals(_node)) {
                RpcClient rpcClient = new RpcClient(hashNode.getIp(), hashNode.getPort());
                try {
                    rpcClient.connect();
                    IHashNodeComm iHashNodeComm = RpcProxy.create(IHashNodeComm.class, rpcClient);
                    if (rpcClient.getStatus() && iHashNodeComm.getCurrentNode().isEarly(_node.getStartTime())) {
                        hashCircle.setMasterNode(hashNode);
                        hashCircle.join(hashNode);
                        return hashCircle;
                    }
                }
                finally {
                    rpcClient.close();
                }
            }
        }
        return hashCircle;
    }

    public static HashCircle createWithNode(HashNode _node, List<HashNode> _nodelist){
        return createWithNode(_node,_nodelist,0);
    }


    /**
     * 根据传入节点列表生成TreeMap结构
     * @param _nodelist
     */
    public void genTreeMap(List<HashNode> _nodelist){
        if(this.nodeList==null)
            this.nodeList = new ConcurrentHashMap<>();
        for(HashNode node:_nodelist){
            long hashKey = HashUtils.hash(node.toString());
            if(this.nodeList.containsKey(hashKey)){
                this.nodeList.replace(hashKey,node);
            }
            else {
                this.nodeList.put(hashKey,node);
            }
        }
    }

    /**
     * 将自己加入到已有的Hash环中
     * @param _masterNode
     */
    public void join(HashNode _masterNode){
        RpcClient rpcClient = new RpcClient(_masterNode.getIp(), _masterNode.getPort());
        try {
            rpcClient.connect();
            if (!rpcClient.getStatus()) {
                log.error("Connect to master node fail");
                return;
            }
            IHashNodeComm iHashNodeComm = RpcProxy.create(IHashNodeComm.class, rpcClient);
            //Add current node to master node list and sync node list
            iHashNodeComm.join(this.currentNode);
            synchronized (this.nodeList) {
                this.nodeList = iHashNodeComm.syncNodeList();
            }
            log.info("Find other circle,join it "+_masterNode.toString());
        }
        finally {
            rpcClient.close();
        }
    }

    public void start() {
        Timer brocastTimer = new Timer();
        brocastTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                brocastNodeList();
                connectNodes();
            }
        },1000,5000);
    }

    public void connectNodes(){
        log.debug("Start maintaince node connections, Total Nodes:"+this.nodeList.size()+" Total Connections:"+this.nodeComms.size());
        this.nodeList.forEach((hk,hN)->{
            if(!this.nodeComms.containsKey(hk)){
                RpcClient rpcClient = new RpcClient(hN.getIp(), hN.getPort());
                rpcClient.connect();
                IHashNodeComm iHashNodeComm = RpcProxy.create(IHashNodeComm.class, rpcClient);
                try {
                    this.rpcClients.putIfAbsent(hk, rpcClient);
                    this.nodeComms.putIfAbsent(hk, iHashNodeComm);
                    log.debug("Init connection from Node:"+currentNode.toString()+" to Node:"+hN.toString());
                }
                catch (Exception ex){
                    rpcClient.close();
                }
            }else {
                IHashNodeComm iHashNodeComm = this.nodeComms.get(hk);
                try{
                    iHashNodeComm.isAlive();
                }
                catch (Exception ex){
                    releaseNode(hk);
                }
            }
        });
       log.debug("End maintaince node connections");
    }

    public void brocastNodeList(){
        log.info("Start brocast nodelist from master");
        if(masterNode==null){
            log.error("Can not determeter master node");
            return;
        }
        if(!currentNode.equals(masterNode)) {
            //从MasterNode获取所有的节点信息
            IHashNodeComm masterNodeComm;
            long hashKey = HashUtils.hash(masterNode.toString());
            if (!nodeComms.containsKey(hashKey)) {
                RpcClient rpcClient = new RpcClient(masterNode.getIp(), masterNode.getPort());
                rpcClient.connect();
                IHashNodeComm iHashNodeComm = RpcProxy.create(IHashNodeComm.class, rpcClient);
                this.rpcClients.putIfAbsent(hashKey,rpcClient);
                this.nodeComms.putIfAbsent(hashKey, iHashNodeComm);
            }
            masterNodeComm = nodeComms.get(hashKey);
            ConcurrentHashMap<Long, HashNode> _nodelist = masterNodeComm.syncNodeList();

            log.debug("Sync nodelist from master "+masterNode.toString()+" nodes:"+_nodelist.size());
            _nodelist.forEach((hk, hN) -> {
                if (!this.nodeList.containsKey(hk)) {
                    this.nodeList.putIfAbsent(hk, hN);
                }
            });

            this.nodeList.forEach((hk, hN) -> {
                if (!_nodelist.containsKey(hk)) {
                    this.releaseNode(hk);
                    this.nodeList.remove(hk);
                }
            });
        }
        else {
            log.debug("I'm master, but I will find identity from others!");
            //不断监测是否有比自己更合适的MasterNode,如果有则更新当前的MasterNode
            this.nodeList.forEach((hk,hN) -> {
                if(!hN.equals(this.currentNode)){
                    IHashNodeComm nodeComm = this.nodeComms.get(hk);
                    if(nodeComm==null){
                        RpcClient rpcClient = new RpcClient(hN.getIp(), hN.getPort());
                        rpcClient.connect();
                        nodeComm = RpcProxy.create(IHashNodeComm.class, rpcClient);
                        if(nodeComm!=null) {
                            this.rpcClients.putIfAbsent(hk,rpcClient);
                            this.nodeComms.putIfAbsent(hk, nodeComm);
                        }
                    }
                    if(nodeComm!=null&&nodeComm.getCurrentNode().isEarly(this.currentNode.getStartTime())){
                        this.masterNode=hN;
                    }
                }
            });
        }
        log.info("End brocast nodelist from master");
    }

    public HashNode getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(HashNode masterNode) {
        this.masterNode = masterNode;
    }

    public RpcServer getRpcServer() {
        return rpcServer;
    }

    public void setRpcServer(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    public HashNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(HashNode currentNode) {
        this.currentNode = currentNode;
    }

    public ConcurrentHashMap<Long, HashNode> getNodeList() {
        return nodeList;
    }

    public void releaseNode(long _hashKey){
        if(this.nodeComms.contains(_hashKey)){
            IHashNodeComm iHashNodeComm = this.nodeComms.get(_hashKey);
            if(iHashNodeComm!=null&&iHashNodeComm.getCurrentNode()!=null){
                RpcClient rpcClient = this.rpcClients.get(_hashKey);
                if(rpcClient!=null)
                    rpcClient.close();
                rpcClients.remove(_hashKey);
                this.nodeComms.remove(_hashKey);
            }
        }
    }
}

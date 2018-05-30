package com.consistenthash.hash;

import java.io.Serializable;

public enum NodeState implements Serializable {

    /**
     * 离线状态
     */
    Offline,
    /**
     * 在线状态
     */
    Online,
    /**
     * 连接超时状态
     */
    Timeout,
    /**
     * 初始化状态
     */
    Init
}

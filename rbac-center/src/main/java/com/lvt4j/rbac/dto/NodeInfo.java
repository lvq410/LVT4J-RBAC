package com.lvt4j.rbac.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 本节点的一些信息
 * @author LV on 2021年3月3日
 */
@Data
public class NodeInfo implements Serializable {
    private static final long serialVersionUID = 2271995066373499814L;
    
    /** 节点IP */
    private final String host;
    /** 节点http端口 */
    private final int port;
    /** 节点hazelcast端口 */
    private final int hazelcastPort;
    /** h2数据库时tcp端口 */
    private final int h2TcpPort;
    /** 节点的启动时间 */
    private final long regTime;
    
    public String address() {
        return host+":"+port;
    }
    
}
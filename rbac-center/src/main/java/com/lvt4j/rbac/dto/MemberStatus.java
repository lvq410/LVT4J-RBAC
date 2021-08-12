package com.lvt4j.rbac.dto;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;

/**
 * 集群成员信息
 * @author LV on 2021年3月4日
 */
@Data
public class MemberStatus implements Serializable {
    private static final long serialVersionUID = -867415935550686842L;
    
    /** 节点ip与服务端口号的组合：ip:${server.port} */
    public String id;
    /** 节点的启动时间 */
    public long regTime;
    /** master/slave/unreachable */
    public String status;
    /** 连接在该节点上的客户端的清单 */
    public Collection<ClientInfo> clients;
    
}
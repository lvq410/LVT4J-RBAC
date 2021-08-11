package com.lvt4j.rbac.dto;

import java.util.Collection;

/**
 * 集群成员信息
 * @author LV on 2021年3月4日
 */
public class MemberStatus {

    /** 节点ip与服务端口号的组合：ip:${server.port} */
    public String id;
    /** 节点的启动时间 */
    public long regTime;
    /** master/slave/unreachable */
    public String status;
    /** 连接在该节点上的客户端的清单 */
    public Collection<ClientInfo> clients;
    
}
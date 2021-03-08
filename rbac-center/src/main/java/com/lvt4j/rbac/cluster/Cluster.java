package com.lvt4j.rbac.cluster;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.dto.ClientInfo;
import com.lvt4j.rbac.dto.MemberStatus;
import com.lvt4j.rbac.dto.NodeInfo;

/**
 * 存储当前集群的信息
 * @author LV on 2021年3月4日
 */
public interface Cluster {

    static final List<OnMasterChangedListener> MasterChangedListeners = new ArrayList<>();
    
    public static void addMasterChangeListener(OnMasterChangedListener listener) {
        MasterChangedListeners.add(listener);
        MasterChangedListeners.sort((l1,l2)->Integer.compare(l1.getOrder(), l2.getOrder()));
    }
    
    /** 本节点是否是master */
    public boolean isLocalMaster();
    
    /** 获取master节点信息，若还未选主，阻塞 */
    public NodeInfo getMasterInfo();
    
//    /** 更新本节点连接的客户端信息 */
//    @Async
//    public void setLocalStatusClients(List<ClientInfo> clients);
    
    @Async
    public void addLocalClient(ClientInfo client);
    @Async
    public void removeLocalClient(ClientInfo client);
    
    /** 获取整个集群状态 */
    public List<MemberStatus> getMemberStats();
    
    /** 向事件总线上发布消息，集群中每个节点都会收到该消息 */
    public void publish(BroadcastMsg4Center msg);
    
}

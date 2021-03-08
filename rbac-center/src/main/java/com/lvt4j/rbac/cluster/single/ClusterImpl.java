package com.lvt4j.rbac.cluster.single;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.cluster.BroadcastMsgHandler;
import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.condition.DbIsSingleOnly;
import com.lvt4j.rbac.dto.ClientInfo;
import com.lvt4j.rbac.dto.MemberStatus;
import com.lvt4j.rbac.dto.NodeInfo;

/**
 *
 * @author LV on 2021年3月4日
 */
@Component
@Conditional(DbIsSingleOnly.class)
public class ClusterImpl implements Cluster {

    @Autowired
    private NodeInfo localNodeInfo;
    
    @Autowired
    private BroadcastMsgHandler broadcastMsgHandler;
    
    private List<ClientInfo> clients = Collections.synchronizedList(new LinkedList<>());
    
    @Override
    public boolean isLocalMaster() {
        return true;
    }

    @Override
    public NodeInfo getMasterInfo() {
        return localNodeInfo;
    }

    @Override
    public void addLocalClient(ClientInfo client) {
        clients.add(client);
    }
    
    @Override
    public void removeLocalClient(ClientInfo client) {
        clients.remove(client);
    }
    
    @Override
    public List<MemberStatus> getMemberStats() {
        MemberStatus s = new MemberStatus();
        s.id = s.address = localNodeInfo.address();
        s.regTime = localNodeInfo.getRegTime();
        s.status = "master";
        s.clients = defaultIfNull(clients, emptyList());
        return Arrays.asList(s);
    }
    
    @Override
    public void publish(BroadcastMsg4Center msg) {
        broadcastMsgHandler.onBroadcastMsg(msg);
    }

}
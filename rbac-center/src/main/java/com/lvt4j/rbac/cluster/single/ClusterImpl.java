package com.lvt4j.rbac.cluster.single;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.cluster.BroadcastMsgHandler;
import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.condition.DbIsSingleOnly;
import com.lvt4j.rbac.dto.MemberStatus;
import com.lvt4j.rbac.dto.NodeInfo;
import com.lvt4j.rbac.service.ClientService;

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
    
    @Autowired
    private ClientService clientService;
    
    @Override
    public boolean isLocalMaster() {
        return true;
    }

    @Override
    public NodeInfo getMasterInfo() {
        return localNodeInfo;
    }

    @Override
    public List<MemberStatus> getMemberShortStats() {
        MemberStatus s = new MemberStatus();
        s.id = localNodeInfo.address();
        s.status = "master";
        return Arrays.asList(s);
    }
    
    @Override
    public List<MemberStatus> getMemberStats() {
        MemberStatus s = new MemberStatus();
        s.id = localNodeInfo.address();
        s.regTime = localNodeInfo.getRegTime();
        s.status = "master";
        s.clients = clientService.getClients();
        return Arrays.asList(s);
    }
    
    @Override
    public void publish(BroadcastMsg4Center msg) {
        broadcastMsgHandler.onBroadcastMsg(msg);
    }

}
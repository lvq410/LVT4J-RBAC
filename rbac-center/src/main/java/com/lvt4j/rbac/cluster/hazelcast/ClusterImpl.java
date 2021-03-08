package com.lvt4j.rbac.cluster.hazelcast;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.hazelcast.cluster.Address;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.topic.ITopic;
import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.condition.DbIsClusterable;
import com.lvt4j.rbac.dto.ClientInfo;
import com.lvt4j.rbac.dto.MemberStatus;
import com.lvt4j.rbac.dto.NodeInfo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2021年3月4日
 */
@Slf4j
@Component
@Conditional(DbIsClusterable.class)
class ClusterImpl implements Cluster, MembershipListener {

    @Autowired
    private NodeInfo localNodeInfo;
    
    @Autowired
    private HazelcastInstance hazelcast;
    
    @Autowired
    private Discover discover;
    
    @Autowired
    private MasterElector masterElector;
    
    private IMap<String, NodeInfo> nodes;
    private MultiMap<String, ClientInfo> clients;
    
    private ITopic<BroadcastMsg4Center> eventBusTopic;
    
    @PostConstruct
    private void init() {
        nodes = hazelcast.getMap("cluster-nodes");
        clients = hazelcast.getMultiMap("cluster-clients");
        eventBusTopic = hazelcast.getTopic("event-bus");
        hazelcast.getCluster().addMembershipListener(this);
        nodes.put(nodeId(localNodeInfo), localNodeInfo);
    }
    
    public boolean isLocalMaster() {
        return localNodeInfo.equals(getMasterInfo());
    }
    
    @SneakyThrows
    public NodeInfo getMasterInfo() {
        return masterElector.getMasterInfo();
    }
    
    @Override
    public void addLocalClient(ClientInfo client) {
        clients.put(nodeId(localNodeInfo), client);
    }
    
    @Override
    public void removeLocalClient(ClientInfo client) {
        clients.remove(nodeId(localNodeInfo), client);
    }
    
    @Override
    public List<MemberStatus> getMemberStats() {
        NodeInfo masterInfo = getMasterInfo();
        String masterId = nodeId(masterInfo);
        return nodes.values().stream().map(n->{
            MemberStatus s = new MemberStatus();
            s.id = nodeId(n);
            s.address = n.address();
            s.regTime = n.getRegTime();
            s.status = masterId.equals(s.id)?"master":"slave";
            s.clients = defaultIfNull(clients.get(s.id), emptyList());
            return s;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void publish(BroadcastMsg4Center msg) {
        eventBusTopic.publish(msg);
    }
    
    private static String nodeId(NodeInfo nodeInfo) {
        return nodeInfo.getHost()+":"+nodeInfo.getHazelcastPort();
    }
    
    @Override
    public void memberAdded(MembershipEvent event) {}
    
    @Override @SneakyThrows
    public void memberRemoved(MembershipEvent event) {
        if(hazelcast.getCluster().getMembers().size()<discover.getQuorum()){
            log.error("集群成员数{}过少<{}，可能脑裂，终止程序", hazelcast.getCluster().getMembers().size(), discover.getQuorum());
            System.exit(-1);
            return;
        }
        Address address = event.getMember().getAddress();
        String id = address.getHost()+":"+address.getPort();
        nodes.remove(id);
        clients.remove(id);
    }
    
}
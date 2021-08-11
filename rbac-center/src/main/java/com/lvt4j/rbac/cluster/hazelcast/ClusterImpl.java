package com.lvt4j.rbac.cluster.hazelcast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.topic.ITopic;
import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.condition.DbIsClusterable;
import com.lvt4j.rbac.dto.MemberStatus;
import com.lvt4j.rbac.dto.NodeInfo;
import com.lvt4j.rbac.service.ClientService;

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

    private static ClusterImpl Instance;
    
    @Autowired
    private NodeInfo localNodeInfo;
    
    @Autowired
    private HazelcastInstance hazelcast;
    
    @Autowired
    private Discover discover;
    
    @Autowired
    private MasterElector masterElector;
    
    @Autowired
    private ClientService clientService;
    
    private IExecutorService clientsCollector;
    
    private ITopic<BroadcastMsg4Center> eventBusTopic;
    
    @PostConstruct
    private void init() {
        clientsCollector = hazelcast.getExecutorService("cluster-clients");
        eventBusTopic = hazelcast.getTopic("event-bus");
        hazelcast.getCluster().addMembershipListener(this);
        Instance = this;
    }
    
    public boolean isLocalMaster() {
        return localNodeInfo.equals(getMasterInfo());
    }
    
    @SneakyThrows
    public NodeInfo getMasterInfo() {
        return masterElector.getMasterInfo();
    }
    
    @Override
    public List<MemberStatus> getMemberShortStats() {
        Set<Member> members = hazelcast.getCluster().getMembers();
        List<MemberStatus> stats = new ArrayList<>(members.size());
        for(Member member : members){
            MemberStatus status = new MemberStatus();
            status.id = member.getAttribute("localIp")+":"+member.getAttribute("server.port");
            status.status = masterElector.getMasterInfo().address().equals(status.id)?"master":"slave";
            stats.add(status);
        }
        return stats;
    }
    
    @Override
    public List<MemberStatus> getMemberStats() throws Throwable {
        Set<Member> members = hazelcast.getCluster().getMembers();
        List<MemberStatus> stats = Collections.synchronizedList(new ArrayList<>(members.size()));
        CountDownLatch latch = new CountDownLatch(stats.size());
        for(Member member : members){
            clientsCollector.submitToMember(new GetStatus(), member, new ExecutionCallback<MemberStatus>() {
                @Override
                public void onResponse(MemberStatus status) {
                    stats.add(status);
                    latch.countDown();
                }
                @Override
                public void onFailure(Throwable e) {
                    log.error("从节点{}加载状态失败", member, e);
                    
                    MemberStatus status = new MemberStatus();
                    status.id = member.getAttribute("localIp")+":"+member.getAttribute("server.port");
                    status.status = "unreachable";
                    status.clients = Collections.emptyList();
                    stats.add(status);
                    latch.countDown();
                }
            });
        }
        latch.await(10, TimeUnit.SECONDS);
        return stats;
    }
    
    @Override
    public void publish(BroadcastMsg4Center msg) {
        eventBusTopic.publish(msg);
    }
    
    private static String nodeId(NodeInfo nodeInfo) {
        return nodeInfo.getHost()+":"+nodeInfo.getPort();
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
    }
    
    static class GetStatus implements Callable<MemberStatus>, Serializable {
        private static final long serialVersionUID = -8571074704095263292L;
        @Override
        public MemberStatus call() throws Exception {
            ClusterImpl service = ClusterImpl.Instance;
            
            NodeInfo n = service.localNodeInfo;
            
            MemberStatus s = new MemberStatus();
            s.id = nodeId(n);
            s.regTime = n.getRegTime();
            s.status = service.isLocalMaster()?"master":"slave";
            s.clients = service.clientService.getClients();
            
            return s;
        }
    }
}
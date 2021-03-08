package com.lvt4j.rbac.cluster.hazelcast;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.ReplicatedMapConfig;
import com.hazelcast.config.SplitBrainProtectionConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.splitbrainprotection.SplitBrainProtectionFunction;
import com.hazelcast.topic.MessageListener;
import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.cluster.BroadcastMsgHandler;
import com.lvt4j.rbac.condition.DbIsClusterable;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月20日
 */
@Slf4j
@Configuration
@Conditional(DbIsClusterable.class)
class HazelcastConfig {

    @Value("${spring.application.name}")
    private String appName;
    
    @Value("${hazelcast.discover.mode}")
    private String discoveryMode;
    
    @Value("${hazelcast.discover.initClusterTimeout}")
    private long initClusterTimeout;
    
    @Value("${localIp}")
    private String host;
    @Value("${hazelcast.port}")
    private int port;
    
    @Autowired
    private Discover discover;
    
    @Autowired @Lazy
    private BroadcastMsgHandler broadcastMsgHandler;
    
    private HazelcastInstance instance;
    
    @Bean
    public HazelcastInstance hazelcast() throws Throwable {
        log.info("Hazelcast discovery mode : {}", discoveryMode);
        log.info("Hazelcast quorum : {}", discover.getQuorum());
        log.info("Hazelcast discovered seeds : {}", discover.getSeeds());
        
        Config config = new Config()
            .setClusterName(appName)
            .setProperty("hazelcast.jmx", "true");
        
        NetworkConfig networkConfig = config.getNetworkConfig()
            .setPort(port)
            .setPublicAddress(host)
            .setPortAutoIncrement(false);
        
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        
        TcpIpConfig tcpIpConfig = new TcpIpConfig()
            .setEnabled(true)
            .setMembers(discover.getSeeds());
        joinConfig.setTcpIpConfig(tcpIpConfig);
        
        SplitBrainProtectionConfig splitBrainProtectionConfig = new SplitBrainProtectionConfig("by-discover", true)
            .setMinimumClusterSize(2)
            .setFunctionImplementation(new SplitBrainProtectionFunction() {
                @Override @SneakyThrows
                public boolean apply(Collection<Member> members) {
                    return members.size()>=discover.getQuorum();
                }
        });
        config.addSplitBrainProtectionConfig(splitBrainProtectionConfig);
        
        ReplicatedMapConfig metaMapConfig = new ReplicatedMapConfig("meta")
            .setSplitBrainProtectionName(splitBrainProtectionConfig.getName());
        config.addReplicatedMapConfig(metaMapConfig);
        
        TopicConfig topicConfig = new TopicConfig("event-bus")
            .setGlobalOrderingEnabled(true);
        MessageListener<BroadcastMsg4Center> topicListener = msg->broadcastMsgHandler.onBroadcastMsg(msg.getMessageObject());
        topicConfig.addMessageListenerConfig(new ListenerConfig(topicListener));
        config.addTopicConfig(topicConfig);
        
        ReplicatedMapConfig clusterNodesMapConfig = new ReplicatedMapConfig("cluster-nodes")
            .setSplitBrainProtectionName(splitBrainProtectionConfig.getName());
        config.addReplicatedMapConfig(clusterNodesMapConfig);
        
        MultiMapConfig clusterClientsMapConfig = new MultiMapConfig("cluster-clients")
            .setBackupCount(0).setAsyncBackupCount(1)
            .setSplitBrainProtectionName(splitBrainProtectionConfig.getName());
        config.addMultiMapConfig(clusterClientsMapConfig);
        
        MapConfig proAuthCacheMapConfig = new MapConfig("pro-auth-*")
            .setBackupCount(0).setAsyncBackupCount(1).setReadBackupData(true)
            .setTimeToLiveSeconds((int)TimeUnit.DAYS.toSeconds(1L));
        config.addMapConfig(proAuthCacheMapConfig);
        
        config.getCPSubsystemConfig()
            .setSessionHeartbeatIntervalSeconds(1)
            .setSessionTimeToLiveSeconds(5)
            .setMissingCPMemberAutoRemovalSeconds(10);
        
        instance = Hazelcast.newHazelcastInstance(config);
        
        waitQuorum();
        
        return instance;
    }
    
    private void waitQuorum() throws Throwable {
        long waited = 0;
        while(waited<initClusterTimeout){
            int memSize = instance.getCluster().getMembers().size();
            int quorum = discover.getQuorum();
            if(memSize>=quorum) return;
            log.warn("等待集群节点数{}达到预计{}", memSize, quorum);
            waited += 500;
            Thread.sleep(500);
        }
        throw new IllegalStateException("等待集群节点数达到预计值超时");
    }
    
    @PreDestroy
    private void destory() {
        if(instance!=null) instance.shutdown();
    }
    
}
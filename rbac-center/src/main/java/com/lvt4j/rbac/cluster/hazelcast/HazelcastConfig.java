package com.lvt4j.rbac.cluster.hazelcast;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.ReplicatedMapConfig;
import com.hazelcast.config.SplitBrainProtectionConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.MessageListener;
import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.cluster.BroadcastMsgHandler;
import com.lvt4j.rbac.condition.DbIsClusterable;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月20日
 */
@Slf4j
@Configuration
@Conditional(DbIsClusterable.class)
class HazelcastConfig {

    static final String SplitBrainProtectionName = "by-discovery";
    
    static final String MetaReplicatedMapName = "_meta";
    
    @Value("${spring.application.name}")
    private String appName;
    
    @Value("${hazelcast.discover.mode}")
    private String discoveryMode;
    
    @Value("${hazelcast.discover.initClusterTimeout}")
    private long initClusterTimeout;

    @Value("${hazelcast.cp.sessionHeartbeatIntervalSeconds:1}")
    private int cpSessionHeartbeatIntervalSeconds;
    @Value("${hazelcast.cp.sessionTimeToLiveSeconds:5}")
    private int cpSessionTimeToLiveSeconds;
    @Value("${hazelcast.cp.missingCPMemberAutoRemovalSeconds:10}")
    private int cpMissingCPMemberAutoRemovalSeconds;
    
    
    @Value("${hazelcast.cache.pro-auth.backupCount:0}")
    private int proAuthBackupCount;
    @Value("${hazelcast.cache.pro-auth.asyncbackupCount:1}")
    private int proAuthAsyncBackupCount;
    @Value("${hazelcast.cache.pro-auth.maxIdleSeconds:86400}")
    private int proAuthMaxIdleSeconds;
    
    @Value("${localIp}")
    private String host;
    @Value("${hazelcast.port}")
    private int port;
    @Value("${server.port}")
    private int serverPort;
    
    @Autowired
    private Discovery discover;
    
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
        
        MemberAttributeConfig attrConfig = new MemberAttributeConfig();
        attrConfig.setAttribute("localIp", host);
        attrConfig.setAttribute("server.port", String.valueOf(serverPort));
        config.setMemberAttributeConfig(attrConfig);
        
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
        
        SplitBrainProtectionConfig splitBrainProtectionConfig = new SplitBrainProtectionConfig(SplitBrainProtectionName, true)
            .setMinimumClusterSize(2)
            .setFunctionImplementation(members->members.size()>=discover.getQuorum());
        config.addSplitBrainProtectionConfig(splitBrainProtectionConfig);
        
        ReplicatedMapConfig metaMapConfig = new ReplicatedMapConfig(MetaReplicatedMapName).setStatisticsEnabled(true)
            .setSplitBrainProtectionName(splitBrainProtectionConfig.getName());
        config.addReplicatedMapConfig(metaMapConfig);
        
        TopicConfig topicConfig = new TopicConfig("event-bus").setStatisticsEnabled(true)
            .setGlobalOrderingEnabled(true);
        MessageListener<BroadcastMsg4Center> topicListener = msg->broadcastMsgHandler.onBroadcastMsg(msg.getMessageObject());
        topicConfig.addMessageListenerConfig(new ListenerConfig(topicListener));
        config.addTopicConfig(topicConfig);
        
        ExecutorConfig executorConfig = new ExecutorConfig("cluster-clients").setStatisticsEnabled(true)
            .setPoolSize(1).setSplitBrainProtectionName(splitBrainProtectionConfig.getName());
        config.addExecutorConfig(executorConfig);
        
        MapConfig proAuthCacheMapConfig = new MapConfig("pro-auth-*")
            .setBackupCount(proAuthBackupCount).setAsyncBackupCount(proAuthAsyncBackupCount).setReadBackupData(true).setStatisticsEnabled(true)
            .setMaxIdleSeconds(proAuthMaxIdleSeconds);
        config.addMapConfig(proAuthCacheMapConfig);
        
        config.getCPSubsystemConfig()
            .setSessionHeartbeatIntervalSeconds(cpSessionHeartbeatIntervalSeconds)
            .setSessionTimeToLiveSeconds(cpSessionTimeToLiveSeconds)
            .setMissingCPMemberAutoRemovalSeconds(cpMissingCPMemberAutoRemovalSeconds);
        
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
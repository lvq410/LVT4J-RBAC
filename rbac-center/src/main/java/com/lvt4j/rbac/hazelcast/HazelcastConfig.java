package com.lvt4j.rbac.hazelcast;

import java.util.Collection;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SplitBrainProtectionConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.splitbrainprotection.SplitBrainProtectionFunction;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月20日
 */
@Slf4j
@Configuration("HazelcastConfiguration")
public class HazelcastConfig {

    @Value("${spring.application.name}")
    private String appName;
    
    @Value("${hazelcast.discover.mode}")
    private String discoveryMode;
    
    @Value("${hazelcast.host}")
    private String host;
    @Value("${hazelcast.port}")
    private int port;
    
    @Autowired
    private Discover discover;
    
    private HazelcastInstance instance;
    
    @Bean
    public HazelcastInstance hazelcast() throws Throwable {
        log.info("Hazelcast discovery mode : {}", discoveryMode);
        log.info("Hazelcast quorum : {}", discover.getQuorum());
        log.info("Hazelcast discovered seeds : {}", discover.getSeeds());
        
        Config config = new Config();
        config.setProperty("hazelcast.jmx", "true");
        config.setClusterName(appName);
        
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(port);
        networkConfig.setPublicAddress(host);
        networkConfig.setPortAutoIncrement(false);
        
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        
        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.setMembers(discover.getSeeds());
        joinConfig.setTcpIpConfig(tcpIpConfig);
        
        SplitBrainProtectionConfig splitBrainProtectionConfig = new SplitBrainProtectionConfig();
        splitBrainProtectionConfig.setName("splitbrain-protector-by-discover");
        splitBrainProtectionConfig.setEnabled(true);
        splitBrainProtectionConfig.setFunctionImplementation(new SplitBrainProtectionFunction() {
            @Override @SneakyThrows
            public boolean apply(Collection<Member> members) {
                return members.size()>=discover.getQuorum();
            }
        });
        config.addSplitBrainProtectionConfig(splitBrainProtectionConfig);
        
        return instance = Hazelcast.newHazelcastInstance(config);
    }
    
    @PreDestroy
    public void destory() {
        if(instance!=null) instance.shutdown();
    }
    
}
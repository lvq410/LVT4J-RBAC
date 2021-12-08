package com.lvt4j.rbac.cluster.hazelcast;

import static com.lvt4j.rbac.cluster.hazelcast.HazelcastConfig.MetaReplicatedMapName;

import java.net.Inet4Address;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapEvent;
import com.hazelcast.replicatedmap.ReplicatedMap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author lichenxi on 2020年7月23日
 */
@Slf4j
@Configuration
@ConfigurationProperties("hazelcast.discover.seed")
@ConditionalOnProperty(name="hazelcast.discover.mode",havingValue="seed")
public class DiscoverySeed implements Discovery {
    
    @Value("${hazelcast.port}")
    private int port;
    
    @Getter@Setter
    private int quorum = -1;
    
    @Getter@Setter
    private List<String> seeds;
    
    @PostConstruct
    private void init() throws Exception {
        if(seeds!=null) return;
        seeds = Arrays.asList(Inet4Address.getLocalHost().getHostAddress()+":"+port);
    }
    
    @Override public void invalidateQuorumCache() {}
    
    @Component
    @Endpoint(id="hazelcast-quorum")
    @ManagedResource(objectName="Discovery:des=quorum查询与设置")
    @ConditionalOnProperty(name="hazelcast.discover.mode",havingValue="seed")
    public static class SeedQuorumActuator implements EntryListener<String, Object> {

        @Autowired
        private HazelcastInstance hazelcast;
        @Autowired
        private DiscoverySeed discovery;
        
        private ReplicatedMap<String, Object> meta;
        
        @PostConstruct
        private void init() {
            meta = hazelcast.getReplicatedMap(MetaReplicatedMapName);
            meta.addEntryListener(this, "quorum");
            Integer quorum = (Integer) meta.get("quorum");
            if(quorum==null){
                log.info("new cluster quorum init by local config {}", discovery.getQuorum());
                meta.put("quorum", discovery.getQuorum());
            }else{
                log.info("join cluster quorum updated to {}", quorum);
                discovery.setQuorum(quorum);
            }
        }
        
        @ReadOperation
        @ManagedAttribute
        public int getQuorum() {
            return discovery.getQuorum();
        }
        
        @WriteOperation
        @ManagedAttribute
        public int setQuorum(int quorum) throws Exception {
            log.info("Hazelcast quorum set manually to {}", quorum);
            meta.put("quorum", quorum);
            discovery.setQuorum(quorum);
            return quorum;
        }

        @Override
        public void entryAdded(EntryEvent<String, Object> event) {
            entryUpdated(event);
        }
        @Override
        public void entryUpdated(EntryEvent<String, Object> event) {
            int quorum = (int) event.getValue();
            int localQuorum = discovery.getQuorum();
            if(quorum!=localQuorum) {
                log.info("local quorum updated from {} to {}", localQuorum, quorum);
                discovery.setQuorum(quorum);
            }
        }

        @Override public void entryRemoved(EntryEvent<String, Object> event) {}
        @Override public void entryEvicted(EntryEvent<String, Object> event) {}
        @Override public void entryExpired(EntryEvent<String, Object> event) {}
        @Override public void mapCleared(MapEvent event) {}
        @Override public void mapEvicted(MapEvent event) {}
    }
    
}
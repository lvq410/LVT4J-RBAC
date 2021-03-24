package com.lvt4j.rbac.cluster.hazelcast.seed;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapEvent;
import com.hazelcast.replicatedmap.ReplicatedMap;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author lichenxi on 2020年5月15日
 */
@Slf4j
@Component
@Endpoint(id="seedQuorum")
@ManagedResource(objectName="SeedQuorum:des=查询与设置quorum")
@ConditionalOnProperty(name="hazelcast.discover.mode",havingValue="seed")
public class SeedQuorumActuator implements EntryListener<String, Object> {

    @Autowired
    private HazelcastInstance hazelcast;
    @Autowired
    private SeedDiscover discover;
    
    private ReplicatedMap<String, Object> meta;
    
    @PostConstruct
    private void init() {
        meta = hazelcast.getReplicatedMap("meta");
        meta.addEntryListener(this, "quorum");
        Integer quorum = (Integer) meta.get("quorum");
        if(quorum==null){
            log.info("新集群，根据配置文件初始化quorum:{}", discover.getQuorum());
            meta.put("quorum", discover.getQuorum());
        }else{
            log.info("加入已有集群，quorum更新为:{}", quorum);
            discover.setQuorum(quorum);
        }
    }
    
    @ReadOperation
    @ManagedOperation
    public int getQuorum() {
        return discover.getQuorum();
    }
    
    @WriteOperation
    @ManagedOperation
    public int setQuorum(int quorum) throws Exception {
        log.info("手动设置quorum为:{}", quorum);
        meta.put("quorum", quorum);
        discover.setQuorum(quorum);
        return quorum;
    }

    @Override
    public void entryAdded(EntryEvent<String, Object> event) {
        entryUpdated(event);
    }
    @Override
    public void entryUpdated(EntryEvent<String, Object> event) {
        int quorum = (int) event.getValue();
        if(quorum!=discover.getQuorum()) log.info("quorum更新为:{}", quorum);;
        discover.setQuorum(quorum);
    }

    @Override
    public void entryRemoved(EntryEvent<String, Object> event) {}
    @Override
    public void entryEvicted(EntryEvent<String, Object> event) {}
    @Override
    public void entryExpired(EntryEvent<String, Object> event) {}
    @Override
    public void mapCleared(MapEvent event) {}
    @Override
    public void mapEvicted(MapEvent event) {}
    
}
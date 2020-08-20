package com.lvt4j.rbac.hazelcast.seed;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author lichenxi on 2020年5月15日
 */
@Slf4j
@Component
@Endpoint(id="hazelcast-quorum")
@ConditionalOnProperty(name="hazelcast.discovery.mode",havingValue="seed")
public class SeedQuorumActuator {

    @Autowired
    private HazelcastInstance hazelcast;
    @Autowired
    private SeedDiscover discover;
    
    private IAtomicReference<Integer> quorumRef;
    
    @PostConstruct
    private void init() {
        quorumRef = hazelcast.getCPSubsystem().getAtomicReference("quorum");
    }
    
    @ReadOperation
    public int getQuorum() {
        return discover.getQuorum();
    }
    
    @WriteOperation
    public int setQuorum(int quorum) throws Exception {
        if(log.isTraceEnabled()) log.trace("hazelcast quorum set to {}", quorum);
        quorumRef.set(quorum);
        discover.setQuorum(quorum);
        return quorum;
    }
    
}
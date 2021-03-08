package com.lvt4j.rbac.cluster.hazelcast.seed;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.lvt4j.rbac.cluster.hazelcast.Discover;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author lichenxi on 2020年7月23日
 */
@Getter@Setter
@Configuration
@ConfigurationProperties("hazelcast.discover.seed")
@ConditionalOnProperty(name="hazelcast.discover.mode",havingValue="seed")
public class SeedDiscover implements Discover {
    
    private int quorum;
    
    private List<String> seeds;
    
}
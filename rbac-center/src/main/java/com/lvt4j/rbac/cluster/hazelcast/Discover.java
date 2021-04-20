package com.lvt4j.rbac.cluster.hazelcast;

import java.util.List;

/**
 * @author LV on 2020年7月23日
 */
public interface Discover {

    public int getQuorum() throws Throwable;
    
    public int getQuorumByCache();
    
    public List<String> getSeeds() throws Throwable;
    
    
    public static boolean isValidMode(String discoverMode) {
        return "rancher".equals(discoverMode) || "seed".equals(discoverMode);
    }
    
}
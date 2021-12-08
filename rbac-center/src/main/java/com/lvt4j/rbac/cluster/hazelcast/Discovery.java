package com.lvt4j.rbac.cluster.hazelcast;

import java.util.List;

/**
 * @author LV on 2020年7月23日
 */
public interface Discovery {

    public int getQuorum();
    
    public void invalidateQuorumCache();
    
    public List<String> getSeeds() throws Throwable;
    
    
    public static boolean isValidMode(String discoverMode) {
        return "rancher".equals(discoverMode) || "seed".equals(discoverMode);
    }
    
}
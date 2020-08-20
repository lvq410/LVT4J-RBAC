package com.lvt4j.rbac.hazelcast;

import java.util.List;

/**
 * @author LV on 2020年7月23日
 */
public interface Discover {

    public int getQuorum() throws Throwable;
    
    public List<String> getSeeds() throws Throwable;
    
}
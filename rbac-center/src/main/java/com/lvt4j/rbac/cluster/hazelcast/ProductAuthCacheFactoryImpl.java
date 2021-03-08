package com.lvt4j.rbac.cluster.hazelcast;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.map.IMap;
import com.lvt4j.rbac.ProductAuthCache;
import com.lvt4j.rbac.UserAuth;
import com.lvt4j.rbac.cluster.ProductAuthCacheFactory;
import com.lvt4j.rbac.condition.DbIsClusterable;

/**
 *
 * @author LV on 2021年3月4日
 */
@Component
@Conditional(DbIsClusterable.class)
public class ProductAuthCacheFactoryImpl implements ProductAuthCacheFactory {

    @Autowired
    private HazelcastInstance hazelcast;

    @Override
    public ProductAuthCache build(String proId) {
        return new HazelcastProductAuthCache(proId);
    }
    
    private class HazelcastProductAuthCache extends ProductAuthCache {

        private IMap<String, UserAuth> map;
        
        private final IAtomicLong hitCount;
        private final IAtomicLong missCount;
        
        private HazelcastProductAuthCache(String proId) {
            map = hazelcast.getMap("pro-auth-"+proId);
            hitCount = hazelcast.getCPSubsystem().getAtomicLong("pro-auth-hit-"+proId);
            missCount = hazelcast.getCPSubsystem().getAtomicLong("pro-auth-miss-"+proId);
        }
        
        @Override
        public UserAuth getOrLoad(String userId) {
            UserAuth orig = map.get(userId);
            if(orig!=null){
                hitCount.incrementAndGetAsync();
                return orig;
            }
            missCount.incrementAndGetAsync();
            orig = loader.apply(userId);
            map.putIfAbsent(userId, orig);
            return orig;
        }
        
        @Override public int getCapacity() { return 0; }
        @Override public int getSize() { return map.size(); }

        @Override public long getHitCount() { return hitCount.get(); }
        @Override public long getMissCount() { return missCount.get(); }
        
        @Override public final void invalidate(String userId) { if(userId==null || userId.isEmpty()) map.clear(); else map.remove(userId); }
        
        @Override public void close() throws IOException {}
    }
    
}
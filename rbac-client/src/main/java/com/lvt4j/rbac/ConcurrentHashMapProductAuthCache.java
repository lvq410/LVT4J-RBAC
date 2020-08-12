package com.lvt4j.rbac;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author LV on 2020年8月12日
 */
class ConcurrentHashMapProductAuthCache extends LocalProductAuthCache {

    private final ConcurrentHashMap<String, UserAuth> map = new ConcurrentHashMap<>();

    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    
    @Override
    public final UserAuth getOrLoad(String userId) {
        return map.compute(userId, (id,orig)->{
            if(orig!=null){
                hitCount.incrementAndGet();
                return orig;
            }
            missCount.incrementAndGet();
            return loader.apply(userId);
        });
    }
    
    @Override public final int getCapacity() { return 0; }
    @Override public int getSize() { return map.size(); }
    @Override public final long getHitCount() { return hitCount.get(); }
    @Override public final long getMissCount() { return missCount.get(); }
    
    @Override public final void invalidate(String userId) { if(userId==null || userId.isEmpty()) map.clear(); else map.remove(userId); }
    
    @Override public final boolean contains(String userId) { return map.containsKey(userId); }
    @Override public final Set<String> cachedUserIds() { return map.keySet(); }
    @Override public final UserAuth put(String userId, UserAuth userAuth) { return map.put(userId, userAuth); }

}

package com.lvt4j.rbac;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author LV on 2020年8月12日
 */
class LruLinkedHashMapProductAuthCache extends LocalProductAuthCache {

    /** 缓存存储的map */
    private final LruLinkedHashMap map;
    
    /** 最大容量 */
    private final int capacity;
    
    private long hitCount;
    private long missCount;
    
    public LruLinkedHashMapProductAuthCache(int capacity) {
        this(true, capacity);
    }
    public LruLinkedHashMapProductAuthCache(boolean supportAsyncRefresh, int capacity) {
        super(supportAsyncRefresh);
        this.capacity = capacity;
        map = new LruLinkedHashMap(capacity);
    }

    @Override
    public synchronized final UserAuth getOrLoad(String userId) {
        UserAuth orig = map.get(userId);
        if(orig!=null){
            hitCount++;
            return orig;
        }
        missCount++;
        UserAuth now = loader.apply(userId);
        if(now!=null) map.put(userId, now);
        return now;
    }
    
    @Override public final int getCapacity() { return capacity; }
    @Override public final int getSize() { return map.size(); }
    @Override public final long getHitCount() { return hitCount; }
    @Override public final long getMissCount() { return missCount; }
    
    @Override public synchronized final void invalidate(String userId) { if(userId==null || userId.isEmpty()) map.clear(); else map.remove(userId); }
    
    @Override public synchronized final boolean contains(String userId) { return map.containsKey(userId); }
    @Override public synchronized final Set<String> cachedUserIds() { return map.keySet(); }
    @Override public synchronized final UserAuth put(String userId, UserAuth userAuth) { return map.put(userId, userAuth); }

    private class LruLinkedHashMap extends LinkedHashMap<String, UserAuth> {
        private static final long serialVersionUID = 1L;
        
        private final int capacity;
        
        public LruLinkedHashMap(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }
        
        @Override
        protected boolean removeEldestEntry(Entry<String, UserAuth> entry) {
            return capacity>0 && size() > capacity;
        }
    }
    
}

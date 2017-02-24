package com.lvt4j.rbac.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 近期最少使用缓存器
 * @author LV
 */
class LruCache<K, V> {

    /** 缓存存储的map */
    private final LruHashMap<K, V> map;

    /** 最大容量 */
    private final int capacity;
    
    /** 使用默认容量10创建缓存器 */
    public LruCache() {this(10);}
    /** 使用指定容量创建缓存器 */
    public LruCache(int capacity) {
        if (capacity<=0) throw new IllegalArgumentException("capacity <= 0");
        this.map = new LruHashMap<K, V>(capacity);
        this.capacity = capacity;
    }

    public final V get(K key) {
        synchronized (this) {
            V value = map.get(key);
            if(value!=null) return value;
        }
        return null;
    }

    public final V put(K key, V value) {
        V previous;
        synchronized (this) {
            previous = map.put(key, value);
            trimToSize(capacity);
        }
        return previous;
    }

    public final V remove(K key) {
        synchronized (this) {
            return map.remove(key);
        }
    }

    public synchronized final void clear() {
        trimToSize(0);
    }

    public int getCapacity() {
        return capacity;
    }
    public synchronized final int getSize() {
        return map.size();
    }

    /** 若大小超过指定大小,移除最久未用数据,调整大小到指定大小内 */
    private void trimToSize(int size) {
        while (true) {
            if(map.size()<=size || map.isEmpty()) break;
            Map.Entry<K, V> toRemove = map.entrySet().iterator().next();
            map.remove(toRemove.getKey());
        }
    }

    @Override
    public synchronized final String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            sb.append(entry.getKey()).append('=')
                    .append(entry.getValue()).append(",");
        }
        sb.append("capacity=").append(capacity).append(",")
            .append("size=").append(map.size());
        return sb.toString();
    }
    private class LruHashMap<KK, VV> extends LinkedHashMap<KK, VV> {
        private static final long serialVersionUID = 1L;
        
        private final int capacity;
        
        public LruHashMap(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }
        
        @Override
        protected boolean removeEldestEntry(Entry<KK, VV> entry) {
            return size() > capacity;
        }
    }
}

package com.lvt4j.rbac.db.lock;

/**
 *
 * @author LV on 2020年5月25日
 */
public interface DbLock {

    default public void readLock(){};
    
    default public void readUnLock(){};
    
    default public void writeLock(){};
    
    default public void writeUnLock(){};
    
}
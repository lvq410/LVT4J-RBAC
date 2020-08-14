package com.lvt4j.rbac.db.lock;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.condition.DbIsSqlite;

/**
 *
 * @author LV on 2020年5月25日
 */
@Component
@Conditional(DbIsSqlite.class)
class SqliteLock implements DbLock {

    private ReentrantLock lock = new ReentrantLock();
    
    @Override
    public void readLock() {
        lock.lock();
    }
    
    @Override
    public void readUnLock() {
        lock.unlock();
    }
    
    @Override
    public void writeLock() {
        lock.lock();
    }
    
    @Override
    public void writeUnLock() {
        lock.unlock();
    }
    
}
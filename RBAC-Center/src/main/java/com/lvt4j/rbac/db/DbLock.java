package com.lvt4j.rbac.db;

import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author LV on 2020年5月25日
 */
@Component
public class DbLock {

    @Value("${db.type}")
    private String dbType;

//    private ReadWriteLock lock;
    private ReentrantLock lock;
    
    @PostConstruct
    private void init() {
        if(!"sqlite".equals(dbType)) return;
//        lock = new ReentrantReadWriteLock();
        lock = new ReentrantLock();
    }
    
    
    public void readLock() {
        if(lock==null) return;
//        lock.readLock().lock();
        lock.lock();
    }
    
    public void readUnLock() {
        if(lock==null) return;
//        lock.readLock().unlock();
        lock.unlock();
    }
    
    public void writeLock() {
        if(lock==null) return;
//        lock.writeLock().lock();
        lock.lock();
    }
    
    public void writeUnLock() {
        if(lock==null) return;
//        lock.writeLock().unlock();
        lock.unlock();
    }
    
}
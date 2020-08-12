package com.lvt4j.rbac.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 单线程运行任务工作器
 * @author LV on 2020年8月5日
 */
@Slf4j
@Component
@ManagedResource(objectName="SingleThreader:des=单线程运行任务工作器")
public class SingleThreader extends Thread {

    /** 为防止推送，用阻塞队列的方式执行 */
    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    
    private volatile boolean destory;
    
    @PostConstruct
    private void init() {
        setName("SingleThreader");
        start();
    }
    
    @PreDestroy
    private void destory() {
        destory = true;
        interrupt();
        try{
            join(1000);
        }catch(InterruptedException e){
            log.warn("SingleThreader close exception", e);
        }
    }
    
    @ManagedOperation
    public int getQueueSize() {
        return queue.size();
    }
    
    @SneakyThrows
    public void enqueue(Runnable runnable) {
        queue.put(runnable);
    }
    
    @Override
    public void run() {
        while(!destory){
            try{
                queue.take().run();
            }catch(Throwable e){
                if(destory) return;
                log.error("队列任务异常", e);
            }
        }
    }
    
}
package com.lvt4j.rbac.cluster.hazelcast;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapEvent;
import com.hazelcast.replicatedmap.ReplicatedMap;
import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.cluster.OnMasterChangedListener;
import com.lvt4j.rbac.condition.DbIsClusterable;
import com.lvt4j.rbac.dto.NodeInfo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 负责尝试成为master
 * 集群中只有一个节点能够成功
 * 其他节点会不断定时重试
 * 当主节点宕机时，其他节点中会有且仅有一个节点自动成为master
 * @author LV on 2021年3月3日
 */
@Slf4j
@Component
@Conditional(DbIsClusterable.class)
class MasterElector extends Thread implements EntryListener<String, Object> {
    
    @Autowired
    private HazelcastInstance hazelcast;
    
    @Autowired
    private NodeInfo localNodeInfo;
    
    private ReplicatedMap<String, Object> meta;
    private Lock masterLock;
    
    private NodeInfo masterInfo;
    
    private volatile boolean destory;
    
    @PostConstruct
    private void init() {
        if(log.isTraceEnabled()) log.trace("启动竞主程序");
        masterLock = hazelcast.getCPSubsystem().getLock("masterLock");
        meta = hazelcast.getReplicatedMap("meta");
        meta.addEntryListener(this, "masterNode");
        setMasterInfo((NodeInfo) meta.get("masterNode"));
        setName("MasterElector");
        start();
    }
    
    @PreDestroy
    private void destory() throws Throwable {
        if(log.isTraceEnabled()) log.trace("退出竞主");
        destory = true;
        interrupt();
        join();
    }
    
    @Override
    public void run() {
        while(!destory) electMaster();
    }
    
    private void electMaster() {
        try{
            masterLock.lockInterruptibly();
                try{
                    if(log.isTraceEnabled()) log.trace("本节点成为主节点");
                    meta.put("masterNode", localNodeInfo);
                    synchronized (this) { wait(); }
                }catch(Throwable e){
                    if(destory) return;
                    if(log.isErrorEnabled()) log.error("主节点初始化异常", e);
                }finally{
                    masterLock.unlock();
                }
        }catch(Throwable ignore){
            if(destory) return;
        }
    }

    @Override
    public void entryAdded(EntryEvent<String, Object> event) {
        setMasterInfo((NodeInfo)event.getValue());
    }
    @Override
    public void entryUpdated(EntryEvent<String, Object> event) {
        setMasterInfo((NodeInfo)event.getValue());
    }

    private void setMasterInfo(NodeInfo masterInfo) {
        if(masterInfo==null) return; //集群中还未有主节点
        if(this.masterInfo==null) { //本节点为新加入节点，还未记录主节点
            this.masterInfo = masterInfo;
            synchronized(this){ notifyAll(); } //释放锁使得阻塞在getMasterInfo上的线程继续执行
            return;
        }
        if(this.masterInfo.equals(masterInfo)) return; //主节点未有变更
        //主节点有变更
        this.masterInfo = masterInfo;
        if(log.isWarnEnabled()) log.warn("主节点变更为:{}", masterInfo.address());
        boolean isLocalMaster = localNodeInfo.equals(masterInfo);
        LinkedList<OnMasterChangedListener> listenersStack = new LinkedList<>();
        for(OnMasterChangedListener listener : Cluster.MasterChangedListeners){
            try{
                listener.beforeMasterChange();
                listenersStack.push(listener);
            }catch(Throwable e){
                log.error("主节点变更，执行重置系统异常，程序退出:{}", listener, e);
                System.exit(-1);
                break;
            }
        }
        while(!listenersStack.isEmpty()){
            OnMasterChangedListener listener = listenersStack.pop();
            try{
                listener.afterMasterChanged(isLocalMaster, masterInfo);
            }catch(Throwable e){
                log.error("主节点变更，执行重置系统异常，程序退出:{}", listener, e);
                System.exit(-1);
                break;
            }
        }
    }
    
    @SneakyThrows
    public NodeInfo getMasterInfo() {
        if(masterInfo!=null) return masterInfo;
        synchronized(this){ wait();  } //初次启动时，等待masterInfo被赋值
        return masterInfo;
    }
    
    @Override
    public void entryRemoved(EntryEvent<String, Object> event) {}
    @Override
    public void entryEvicted(EntryEvent<String, Object> event) {}
    @Override
    public void entryExpired(EntryEvent<String, Object> event) {}
    @Override
    public void mapCleared(MapEvent event) {}
    @Override
    public void mapEvicted(MapEvent event) {}
    
}
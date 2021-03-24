package com.lvt4j.rbac.service;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lvt4j.rbac.Utils.Scheduler;
import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.cluster.OnMasterChangedListener;
import com.lvt4j.rbac.dao.OpLogMapper;
import com.lvt4j.rbac.db.lock.DbLock;
import com.lvt4j.rbac.dto.NodeInfo;
import com.lvt4j.rbac.po.OpLog;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年6月8日
 */
@Slf4j
@Service
class OpLogCleaner extends Scheduler implements OnMasterChangedListener {

    @Value("${oplog.maxdays}")
    private int maxDays;
    
    @Autowired
    private Cluster cluster;
    @Autowired
    private DbLock dbLock;
    
    @Autowired
    private OpLogMapper mapper;
    
    @PostConstruct
    private void init() {
        Cluster.addMasterChangeListener(this);
        initCleaner();
    }
    private void initCleaner() {
        if(!cluster.isLocalMaster()) return;
        initAsMaster();
    }
    private void initAsMaster() {
        initScheduler("0 0 0 * * *", this::clean, "OpLogCleaner");
    }
    
    @PreDestroy
    private void destory() {
        destoryScheduler();
    }
    
    @Override public int getOrder() { return Order_OpLogCleaner; }
    @Override
    public void beforeMasterChange() throws Throwable {
        destory();
    }
    @Override
    public void afterMasterChanged(boolean isLocalMaster, NodeInfo masterInfo) throws Throwable {
        initCleaner();
    }
    
    private void clean(){
        dbLock.writeLock();
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-maxDays);
            Date date = calendar.getTime();
            int count = mapper.delete(OpLog.Query.builder().timeCeiling(date).build().toWrapperWithoutSort());
            log.info("清理操作日志{}条", count);
        }catch(Throwable e){
            log.error("清理操作日志异常", e);
        }finally{
            dbLock.writeUnLock();
        }
    }
    
}
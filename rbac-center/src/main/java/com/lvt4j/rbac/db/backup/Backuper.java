package com.lvt4j.rbac.db.backup;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import lombok.extern.slf4j.Slf4j;

/**
 * 嵌入式数据库需要定时备份
 * @author LV on 2020年8月5日
 */
@Slf4j
abstract class Backuper {

    @Value("${db.backup.cron}")
    protected String cron;
    @Value("${db.backup.folder}")
    protected String folder;
    @Value("${db.backup.max}")
    protected int max;
    
    private ScheduledExecutorService scheduler;
    
    @PostConstruct
    private void init() {
        if(!folder.endsWith("/")) folder += "/";
    }
    
    protected final void initScheduler(Runnable backup) {
        scheduler = Executors.newScheduledThreadPool(1);
        new ConcurrentTaskScheduler(scheduler)
            .schedule(()->{cleanOldBackup();backup.run();}, new CronTrigger(cron));
    }
    
    private void cleanOldBackup() {
        try{
            File folder = new File(this.folder);
            if(!folder.exists()) return;
            List<Pair<Long, File>> baks = Stream.of(folder.listFiles()).map(f->Pair.of(f.lastModified(), f)).collect(toList());
            baks.sort((a,b)->{
                return Long.compare(a.getKey(), b.getKey());
            });
            while(baks.size()>max){
                baks.get(0).getValue().delete();
                baks.remove(0);
            }
        }catch(Exception e){
            log.error("Clean old backup ex", e);
        }
    }
    
    @PreDestroy
    private void destory() {
        if(scheduler!=null) scheduler.shutdownNow();
    }
    
}

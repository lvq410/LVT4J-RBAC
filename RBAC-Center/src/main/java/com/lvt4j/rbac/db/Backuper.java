package com.lvt4j.rbac.db;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.lvt4j.basic.TDB;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据库备份
 * @author LV
 */
@Slf4j
@Configuration("Backuper")
class Backuper {

    @Value("${db.type}")
    private String dbType;
    @Value("${db.folder}")
    private String dbFolder;
    
    @Value("${db.backup.cron}")
    private String cron;
    @Value("${db.backup.folder}")
    private String folder;
    @Value("${db.backup.max}")
    private int max;
    
    @Value("${db.h2.master}")
    private boolean h2Master;
    
    @Autowired
    private TDB db;
    
    private ScheduledExecutorService scheduler;
    
    @PostConstruct
    private void init() {
        if(!dbFolder.endsWith("/")) dbFolder += "/";
        if(!folder.endsWith("/")) folder += "/";
        switch(dbType){
        case "sqlite": initSqliteBackup(); break;
        case "h2": initH2Backup(); break;
        default: break;
        }
    }
    private void initSqliteBackup() {
        log.info("Sqlite数据库定时{}备份于:{}", cron, folder);
        initScheduler(this::sqliteBackup);
    }
    private void initH2Backup() {
        if(!h2Master) return;
        log.info("H2数据库定时{}备份于:{}", cron, folder);
        initScheduler(this::h2Backup);
    }
    
    private void initScheduler(Runnable backup) {
        scheduler = Executors.newScheduledThreadPool(1);
        new ConcurrentTaskScheduler(scheduler)
            .schedule(backup, new CronTrigger(cron));
    }
    
    @PreDestroy
    private void destory() {
        if(scheduler!=null) scheduler.shutdownNow();
    }
    
    private void sqliteBackup() {
        cleanOldBackup();
        try{
            File db = new File(dbFolder, "rbac.db");
            File backup = new File(folder, DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd_HH-mm-ss")+".sqlite.db");
            FileUtils.copyFile(db, backup, false);
        }catch(Exception e){
            log.error("Sqlite backup ex", e);
        }
        
    }
    private void h2Backup() {
        cleanOldBackup();
        try{
            String backup = folder+DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd_HH-mm-ss")+".h2.zip";
            db.executeSQL("backup to ?", backup).execute();
        }catch(Exception e){
            log.error("h2 backup ex", e);
        }
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
}
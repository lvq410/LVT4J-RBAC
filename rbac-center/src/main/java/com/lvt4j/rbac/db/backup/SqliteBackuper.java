package com.lvt4j.rbac.db.backup;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.condition.DbIsSqlite;
import com.lvt4j.rbac.condition.IsMaster;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月5日
 */
@Slf4j
@Component
@Conditional({DbIsSqlite.class, IsMaster.class})
class SqliteBackuper extends Backuper {

    @Value("${db.folder}")
    private String dbFolder;
    
    @PostConstruct
    private void init() {
        if(!dbFolder.endsWith("/")) dbFolder += "/";
        log.info("Sqlite数据库定时{}备份于:{}", cron, folder);
        initScheduler(this::backup);
    }
    
    private void backup() {
        try{
            File db = new File(dbFolder, "rbac.db");
            File backup = new File(folder, DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd_HH-mm-ss")+".sqlite.db");
            FileUtils.copyFile(db, backup, false);
        }catch(Exception e){
            log.error("Sqlite backup ex", e);
        }
        
    }
    
}
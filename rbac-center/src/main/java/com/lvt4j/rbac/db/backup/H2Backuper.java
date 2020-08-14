package com.lvt4j.rbac.db.backup;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.condition.DbIsH2;
import com.lvt4j.rbac.condition.IsMaster;
import com.lvt4j.rbac.dao.ManageMapper;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月5日
 */
@Slf4j
@Component
@Conditional({DbIsH2.class,IsMaster.class})
class H2Backuper extends Backuper {

    @Autowired
    private ManageMapper mapper;
    
    @PostConstruct
    private void init() {
        log.info("H2数据库定时{}备份于:{}", cron, folder);
        initScheduler(this::backup);
    }
    
    private void backup() {
        try{
            String backup = folder+DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd_HH-mm-ss")+".h2.zip";
            mapper.backup(backup);
        }catch(Exception e){
            log.error("h2 backup ex", e);
        }
    }
    
}

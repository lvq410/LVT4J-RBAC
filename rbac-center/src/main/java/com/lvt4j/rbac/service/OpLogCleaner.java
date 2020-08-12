//package com.lvt4j.rbac.service;
//
//import java.util.Calendar;
//import java.util.Date;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Conditional;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import com.lvt4j.basic.TDB;
//import com.lvt4j.basic.TDB.Table;
//import com.lvt4j.rbac.condition.IsMaster;
//import com.lvt4j.rbac.data.OpLog;
//import com.lvt4j.rbac.db.lock.DbLock;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// *
// * @author LV on 2020年6月8日
// */
//@Slf4j
//@Service
//@EnableScheduling
//@Conditional(IsMaster.class)
//class OpLogCleaner {
//
//    @Value("${oplog.maxdays}")
//    private int maxDays;
//    
//    @Autowired
//    private DbLock dbLock;
//    @Autowired
//    private TDB db;
//    
//    @Scheduled(cron="0 0 0 * * *")
//    public void clean(){
//        dbLock.writeLock();
//        try{
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MILLISECOND, 0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-maxDays);
//            Date date = calendar.getTime();
//            String oplogTbl = OpLog.class.getAnnotation(Table.class).value();
//            int count = db.executeSQL("delete from "+oplogTbl+" where time < ?", date).execute();
//            log.info("清理操作日志{}条", count);
//        }catch (Throwable e) {
//            log.error("清理操作日志异常", e);
//        }finally{
//            dbLock.writeUnLock();
//        }
//    }
//    
//}
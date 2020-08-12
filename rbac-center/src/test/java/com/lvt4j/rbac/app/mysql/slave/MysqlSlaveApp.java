package com.lvt4j.rbac.app.mysql.slave;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2020年8月11日
 */
public class MysqlSlaveApp {

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "mysqlslave");
        RbacCenterApp.main(args);
    }
    
}

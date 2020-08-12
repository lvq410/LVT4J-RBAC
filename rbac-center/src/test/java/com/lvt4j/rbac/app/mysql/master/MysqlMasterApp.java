package com.lvt4j.rbac.app.mysql.master;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2020年8月11日
 */
public class MysqlMasterApp {

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "mysqlmaster");
        RbacCenterApp.main(args);
    }
    
}

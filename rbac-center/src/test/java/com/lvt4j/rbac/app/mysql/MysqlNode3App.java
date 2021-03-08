package com.lvt4j.rbac.app.mysql;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2020年8月11日
 */
public class MysqlNode3App {

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "mysql-node3,log");
        RbacCenterApp.main(args);
    }
    
}

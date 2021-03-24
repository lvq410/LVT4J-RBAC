package com.lvt4j.rbac.app.mysql;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2020年8月11日
 */
public class MysqlNode1App {

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "mysql-node1,log");
        RbacCenterApp.main(args);
    }
    
}

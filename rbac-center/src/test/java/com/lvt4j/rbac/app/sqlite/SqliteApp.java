package com.lvt4j.rbac.app.sqlite;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2020年8月5日
 */
public class SqliteApp {

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "sqlite");
        RbacCenterApp.main(args);
    }
    
}

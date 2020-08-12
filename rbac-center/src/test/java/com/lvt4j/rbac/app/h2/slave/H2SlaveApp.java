package com.lvt4j.rbac.app.h2.slave;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2020年5月26日
 */
public class H2SlaveApp {

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "h2slave");
        RbacCenterApp.main(args);
    }
    
}

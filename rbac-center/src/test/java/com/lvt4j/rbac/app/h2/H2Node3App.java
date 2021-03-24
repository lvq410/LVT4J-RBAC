package com.lvt4j.rbac.app.h2;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2020年5月26日
 */
public class H2Node3App {

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "h2-node3,log");
        RbacCenterApp.main(args);
    }
    
}

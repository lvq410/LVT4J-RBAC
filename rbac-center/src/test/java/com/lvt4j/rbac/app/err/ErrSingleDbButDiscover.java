package com.lvt4j.rbac.app.err;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2021年3月8日
 */
public class ErrSingleDbButDiscover {

    public static void main(String[] args) throws Throwable {
        System.setProperty("spring.profiles.active", "err-singledb-discover");
        RbacCenterApp.main(args);
    }
    
}

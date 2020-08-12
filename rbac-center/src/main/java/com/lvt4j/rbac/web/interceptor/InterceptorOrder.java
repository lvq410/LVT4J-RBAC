package com.lvt4j.rbac.web.interceptor;

/**
 *
 * @author LV
 */
interface InterceptorOrder {

    public static final int Admin = 0;
    
    public static final int SQLiteDbLock = Admin +1;
    
    public static final int CurPro4View = SQLiteDbLock +1;
    
}

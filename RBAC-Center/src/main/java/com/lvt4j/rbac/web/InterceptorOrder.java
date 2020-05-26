package com.lvt4j.rbac.web;

/**
 *
 * @author LV
 */
interface InterceptorOrder {

    public static final int Admin = 0;
    
    public static final int SQLiteDbLock = Admin +1;
    
    public static final int CurPro4View = SQLiteDbLock +1;
    
    public static final int Transaction = CurPro4View +1;
    
}

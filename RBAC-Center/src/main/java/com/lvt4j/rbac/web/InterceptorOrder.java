package com.lvt4j.rbac.web;

/**
 *
 * @author LV
 */
public class InterceptorOrder {

    public static final int Admin = 0;
    public static final int SQLite = Admin +1;
    public static final int CurPro4View = SQLite +1;
    public static final int Transaction = CurPro4View +1;
    
}

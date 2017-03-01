package com.lvt4j.rbac;

import java.io.File;

import net.sf.json.JSONObject;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TPager;

/**
 * 各种全局常量
 * @author lichenxi
 */
public class Consts {
    
    /** 根java包 */
    public static final String BasePackage = Consts.class.getPackage().getName();
    
    /** app根文件夹 */
    public static final File AppFolder = new File(System.getProperty("user.dir"));
    
    /** 数据库文件名 */
    public static final String DBFileName = "rbac.db";
    public static TDB DB;
    
    public static final Class<?>[] SupportHandlerMethodTypes = new Class<?>[]{
        JSONObject.class, TPager.class, String[].class};
    
    /** 各种错误码 */
    public static final class Err {
        public static final int NotFound = 404;
    }
    
}

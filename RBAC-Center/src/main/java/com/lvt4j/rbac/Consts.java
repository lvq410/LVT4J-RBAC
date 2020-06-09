package com.lvt4j.rbac;

import java.io.File;
import java.util.Date;

import com.lvt4j.basic.TPager;

import net.sf.json.JSONObject;

/**
 * 各种全局常量
 * @author LV
 */
public class Consts {
    
    /** 根java包 */
    public static final String BasePackage = Consts.class.getPackage().getName();
    
    /** app根文件夹 */
    public static final File AppFolder = new File(System.getProperty("user.dir"));
    /** config文件夹 */
    public static final File ConfFolder = new File(AppFolder, "config");
    /** web文件夹 */
    public static final File WebFolder = new File(AppFolder, "web");
    /** vm文件夹 */
    public static final File VMFolder = new File(WebFolder, "vm");
    
    public static final String CookieName_CurProAutoId= "RbacCurProAutoId";
    public static final String CookieName_Auth= "RbacAuth";
    
    static{
        ConfFolder.mkdirs();
    }
    
    public static final Class<?>[] SupportHandlerMethodTypes = new Class<?>[]{
        JSONObject.class, TPager.class, String[].class, int[].class, Date.class};
    
    /** 各种错误码 */
    public static final class ErrCode{
        public static final int NotFound = 404;
        public static final int Duplicate = 501;
    }
    
}
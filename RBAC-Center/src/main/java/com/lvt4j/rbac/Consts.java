package com.lvt4j.rbac;

import java.io.File;

import net.sf.json.JSONObject;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TPager;
import com.lvt4j.rbac.service.Dao;

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
    /** res文件夹 */
    public static final File ResFolder = new File(AppFolder, "res");
    
    
    /** 数据库文件名 */
    public static final String DBFileName = "rbac.db";
    public static TDB DB;
    public static Dao Dao;
    
    public static final Class<?>[] SupportHandlerMethodTypes = new Class<?>[]{
        JSONObject.class, TPager.class, String[].class, int[].class};
    
    /** velocity的模板文件相对路径 */
    public static final String VelocityTplContextPath = "web/vm/";
    
    /** 各种错误码 */
    public static final class ErrCode{
        public static final int NotFound = 404;
        public static final int Duplicate = 501;
    }
    
}

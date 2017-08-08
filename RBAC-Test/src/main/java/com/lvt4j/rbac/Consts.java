package com.lvt4j.rbac;

import java.io.File;


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
    public static final File ConfFolder = new File(Consts.AppFolder, "config");
}

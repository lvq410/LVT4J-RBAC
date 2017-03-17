package com.lvt4j.rbac;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

/**
 * @author LV
 */
public class Config {
    
    private static final PropertiesConfiguration Props;
    
    static{
        try {
            File propFile = new File(Consts.ConfFolder, "application.properties");
            Props = new PropertiesConfiguration();
            Props.setEncoding("utf-8");
            Props.setFile(propFile);
            Props.setReloadingStrategy(new FileChangedReloadingStrategy());
            Props.load();
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件application.properties失败!", e);
        }
    }
    
    public static String adminUser(){return Props.getString("admin.user");}
    public static String adminPwd(){return Props.getString("admin.pwd");}
    
}

package com.lvt4j.rbac;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author LV
 */
@Configuration("AdminConfig")
public class AdminConfig {
    
    @Autowired
    private Environment env;
    
    private PropertiesConfiguration props;
    
    @PostConstruct
    private void init() throws Exception {
        File propFile = new File(Consts.ConfFolder, "application.properties");
        if(!propFile.exists()) initAdminFile(propFile);
        props = new PropertiesConfiguration();
        props.setEncoding("utf-8");
        props.setFile(propFile);
        props.setReloadingStrategy(new FileChangedReloadingStrategy());
        props.load();
        
    }
    private void initAdminFile(File propFile) throws Exception {
        InputStream is = AdminConfig.class.getResourceAsStream("admin.properties");
        FileUtils.copyInputStreamToFile(is, propFile);
    }
    
    public boolean isAdmin(String userId, String pwd) {
        if(isBlank(userId) || isBlank(userId)) return false;
        if(isAdminInProp(userId, pwd)) return true;
        if(isAdminInSpring(userId, pwd)) return true;
        return false;
    }
    private boolean isAdminInProp(String userId, String pwd) {
        String storedPwd = props.getString("admin."+userId);
        if(isBlank(storedPwd)) return false;
        if(!storedPwd.equals(pwd)) return false;
        return true;
    }
    private boolean isAdminInSpring(String userId, String pwd) {
        String storedPwd = env.getProperty("admin."+userId);
        if(isBlank(storedPwd)) return false;
        if(!storedPwd.equals(pwd)) return false;
        return true;
    }
}
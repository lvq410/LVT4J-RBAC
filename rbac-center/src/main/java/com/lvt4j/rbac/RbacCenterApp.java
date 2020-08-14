package com.lvt4j.rbac;

import java.io.File;
import java.net.Inet4Address;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author LV
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class RbacCenterApp implements WebMvcConfigurer{

    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("localIp", Inet4Address.getLocalHost().getHostAddress());
        
        SpringApplication app = new SpringApplication(RbacCenterApp.class);
        File applicationPidFile = new File(Consts.AppFolder, "applicationPid");
        applicationPidFile.createNewFile();
        applicationPidFile.deleteOnExit();
        ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter(applicationPidFile);
        app.addListeners(applicationPidFileWriter);
        app.run(args);
    }
    
}
package com.lvt4j.rbac;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;

/**
 *
 * @author LV
 */
@SpringBootApplication
public class Main{

    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding", "UTF-8");
        
        SpringApplication app = new SpringApplication(Main.class);
        File applicationPidFile = new File(Consts.AppFolder, "applicationPid");
        applicationPidFile.createNewFile();
        applicationPidFile.deleteOnExit();
        ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter(applicationPidFile);
        app.addListeners(applicationPidFileWriter);
        app.run(args);
    }
    
}

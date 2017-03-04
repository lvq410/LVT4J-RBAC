package com.lvt4j.rbac;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.lvt4j.basic.TFile;

/**
 *
 * @author LV
 */
@SpringBootApplication
public class Main{

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("App路径:"+Consts.AppFolder.getAbsolutePath());
        File dbFile = new File(Consts.AppFolder, Consts.DBFileName);
        File confFile = new File(Consts.ConfFolder, "application.properties");
        boolean isInit = !dbFile.exists() || !Consts.ConfFolder.exists() || !confFile.exists();
        if(isInit) {
            TFile.write(dbFile, Main.class.getClassLoader().getResourceAsStream(Consts.DBFileName));
            Consts.ConfFolder.mkdirs();
            TFile.write(confFile, Main.class.getClassLoader().getResourceAsStream("/config/release/application.properties"));
            System.out.println("初次启动，请检查配置文件，如修改服务端口等");
            System.exit(0);
        }
        
        SpringApplication.run(Main.class, args);
    }
    
}

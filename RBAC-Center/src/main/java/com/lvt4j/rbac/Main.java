package com.lvt4j.rbac;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.lvt4j.basic.TFile;

/**
 *
 * @author lichenxi
 */
@SpringBootApplication
public class Main{

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        
        System.out.println("App路径:"+Consts.AppFolder.getAbsolutePath());
        File dbFile = new File(Consts.AppFolder, Consts.DBFileName);
        if(!dbFile.exists())
            TFile.write(dbFile, Main.class.getClassLoader().getResourceAsStream(Consts.DBFileName));
        
        SpringApplication.run(Main.class, args);
    }
    
}

package com.lvt4j.rbac;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LV on 2020年8月10日
 */
public class ProductAuthClientTest {

    public static void cmd(Level level, String proId, String centerAddr) throws Exception {
        Logger log = Logger.getLogger(ProductAuthClient.class.getName());
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        log.addHandler(handler);
        log.setLevel(level);
        
        ProductAuthClient client = new ProductAuthClient(proId, centerAddr);
        
        String line = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while((line=reader.readLine())!=null){
            if(line.isEmpty()) continue;
            try{
                String[] ss = line.split(" ");
                
                if("get".equals(ss[0])){
                    System.out.println(client.getUserAuth(ss[1]));
                }else if("stats".equals(ss[0])){
                    System.out.println(client.getSize()+"/"+client.getCapacity()+" "+client.getHitCount()+"/"+client.getMissCount()+" "+client.getLoadSuccCount()+"/"+client.getLoadFailCount());
                }else if("close".equals(ss[0])){
                    client.close();
                    return;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        cmd(Level.ALL, "test", "127.0.0.1:80");
        
    }
    
}

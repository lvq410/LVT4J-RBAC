package com.lvt4j.rbac;

import static java.util.logging.Level.ALL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 *
 * @author LV on 2020年8月10日
 */
public class ProductAuthClientTest {

    public static void main(String[] args) throws Exception {
        Logger log = Logger.getLogger(ProductAuthClient.class.getName());
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(ALL);
        log.addHandler(handler);
        log.setLevel(ALL);
        
        ProductAuthClient client = new ProductAuthClient("test", "127.0.0.1:80");
        
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
    
}

package com.lvt4j.rbac.app.clients;

import java.util.logging.Level;

import com.lvt4j.rbac.ProductAuthClientTest;

/**
 *
 * @author LV on 2021年3月4日
 */
public class Client_test_node1 {

    public static void main(String[] args) throws Exception {
        ProductAuthClientTest.cmd(Level.ALL, "test", "127.0.0.1:10180");
    }
    
}

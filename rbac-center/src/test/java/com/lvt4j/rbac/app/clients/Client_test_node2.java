package com.lvt4j.rbac.app.clients;

import java.util.logging.Level;

import com.lvt4j.rbac.ProductAuthClientTest;

/**
 *
 * @author LV on 2021年3月5日
 */
public class Client_test_node2 {

    public static void main(String[] args) throws Throwable {
        ProductAuthClientTest.cmd(Level.ALL, "test", "127.0.0.1:10280");
    }
    
}

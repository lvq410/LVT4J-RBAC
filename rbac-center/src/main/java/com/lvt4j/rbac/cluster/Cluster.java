package com.lvt4j.rbac.cluster;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.HazelcastInstance;

/**
 *
 * @author LV on 2020年8月21日
 */
public class Cluster extends Thread {

    @Autowired
    private HazelcastInstance hazelcast;
    
    
    
    @Override
    public void run() {
    }
    
    public static void main(String[] args) {
        int a = 1000;
                Integer b = 1000;
                System.out.println(a==b);
    }
    
}

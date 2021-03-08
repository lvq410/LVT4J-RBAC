package com.lvt4j.rbac;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

/**
 *
 * @author LV on 2021年3月4日
 */
@Service
public class ConsoleExiter extends Thread {

    @PostConstruct
    private void init() {
        setName("ConsoleExiter");
        setDaemon(true);
        start();
    }
    
    @Override @SneakyThrows
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        reader.readLine();
        System.exit(0);
    }
    
}
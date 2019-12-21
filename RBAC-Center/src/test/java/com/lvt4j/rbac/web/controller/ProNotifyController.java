package com.lvt4j.rbac.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.db.Write;
import com.lvt4j.rbac.service.Dao;

/**
 *
 * @author LV
 */
@RestController
@RequestMapping("/proNotify")
public class ProNotifyController {

    @Autowired
    Dao dao;
    
    @Write
    @RequestMapping
    public void notify(
            @RequestParam int proAutoId) {
        dao.productNotify(proAutoId);
    }
    
}
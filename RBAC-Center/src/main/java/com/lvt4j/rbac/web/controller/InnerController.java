package com.lvt4j.rbac.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.ProductAuthImp;
import com.lvt4j.rbac.service.ProductAuthCache;

@RestController
@RequestMapping("/inner")
public class InnerController{

    @Autowired
    ProductAuthCache productAuthCache;
    
    @RequestMapping("/proModify")
    public long proModify(
            @RequestParam String proId) {
        ProductAuthImp productAuth = productAuthCache.get(proId);
        if(productAuth==null) return System.currentTimeMillis();
        return productAuth.product.lastModify;
    }
    
    @RequestMapping("/userAuth")
    public byte[] userAuth(
            @RequestParam String proId,
            @RequestParam String userId) throws Exception {
        ProductAuthImp productAuth = productAuthCache.get(proId);
        if(productAuth==null) return null;
        return productAuth.userAuthSerialize(userId);
    }
    
}

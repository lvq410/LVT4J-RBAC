package com.lvt4j.rbac.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.basic.TBaseDataConvert;
import com.lvt4j.basic.TStream;
import com.lvt4j.rbac.ProductAuth4Center;
import com.lvt4j.rbac.UserAuth;
import com.lvt4j.rbac.service.ProductAuthCache;

@RestController
@RequestMapping("/inner")
public class InnerController{

    @Autowired
    ProductAuthCache productAuthCache;
    
    @RequestMapping("/proLastModify")
    public byte[] proModify(
            @RequestParam String proId) {
        ProductAuth4Center productAuth = productAuthCache.get(proId);
        if(productAuth==null) return TBaseDataConvert.long2ByteS(0L);
        return TBaseDataConvert.long2ByteS(productAuth.product.lastModify);
    }
    
    @RequestMapping("/userAuth")
    public byte[] userAuth(
            @RequestParam String proId,
            @RequestParam(required=false) String userId) throws Exception {
        UserAuth userAuth = null;
        ProductAuth4Center productAuth = productAuthCache.get(proId);
        if(productAuth==null){ //产品为空返回空的用户权限
            userAuth = new UserAuth();
            userAuth.userId = userId;
            userAuth.exist = false;
        } else {
            userAuth = productAuth.getUserAuth(userId);
            //用户未在授权中心注册,因为直接就是游客权限,不用传输,序列化前克隆一遍后移除权限信息
            if(userId!=null && !userId.isEmpty() && !userAuth.exist){
                UserAuth origUserAuth = userAuth;
                userAuth = new UserAuth();
                userAuth.userId = origUserAuth.userId;
                userAuth.exist = origUserAuth.exist;
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(userAuth);
        byte[] bytes = baos.toByteArray();
        return TStream.compress(bytes);
    }
    
}

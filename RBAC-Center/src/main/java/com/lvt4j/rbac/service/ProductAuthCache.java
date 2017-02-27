package com.lvt4j.rbac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TLruCache;
import com.lvt4j.rbac.ProductAuthImp;
import com.lvt4j.rbac.data.bean.Product;


/**
 *
 * @author lichenxi
 */
@Service
public class ProductAuthCache {

    /** 放在缓存中,表示不存在的产品 */
    private static final ProductAuthImp Absent = new ProductAuthImp(null);
    
    @Autowired
    TDB db;
    
    /** 所有产品缓存 */
    TLruCache<String, ProductAuthImp> caches = new TLruCache<String, ProductAuthImp>(1000);
    
    /** 获取指定产品的权限,若产品不存在,返回null */
    public ProductAuthImp get(String proId) {
        ProductAuthImp productAuth = caches.get(proId);
        if(productAuth!=null) return productAuth;
        synchronized (caches) {
            productAuth = caches.get(proId);
            if(productAuth!=null) return Absent==productAuth?null:productAuth;
            Product product = db.select("select * from product where id=?", proId).execute2ModelOne(Product.class);
            productAuth = product==null?Absent:new ProductAuthImp(product);
            caches.put(proId, productAuth);
            return Absent==productAuth?null:productAuth;
        }
    }
    
    /** 获取指定产品的权限,若未缓存,返回null */
    public ProductAuthImp getIfPresetn(String proId) {
        ProductAuthImp productAuth = caches.get(proId);
        if(Absent==productAuth) return null;
        return productAuth;
    }
    
    public void invalidate(String proId) {
        caches.remove(proId);
    }
    
}

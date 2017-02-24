package com.lvt4j.rbac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TLruCache;
import com.lvt4j.rbac.data.ProductAuthImp;
import com.lvt4j.rbac.data.bean.Product;


/**
 *
 * @author lichenxi
 */
@Service
public class Cache {

    /** 放在缓存中,表示不存在的产品 */
    private static final ProductAuthImp AbsentProductAuth = new ProductAuthImp(null);
    
    @Autowired
    TDB db;
    
    TLruCache<String, ProductAuthImp> productAuthCache = new TLruCache<String, ProductAuthImp>(1000);
    
    /** 获取指定产品的权限,若不存在,返回null */
    public ProductAuthImp getProductAuth(String proId) {
        ProductAuthImp productAuth = productAuthCache.get(proId);
        if(productAuth!=null) return productAuth;
        synchronized (productAuthCache) {
            productAuth = productAuthCache.get(proId);
            if(productAuth!=null) return AbsentProductAuth==productAuth?null:productAuth;
            Product product = db.select("select * from product where id=?", proId).execute2ModelOne(Product.class);
            productAuth = product==null?AbsentProductAuth:new ProductAuthImp(product);
            productAuthCache.put(proId, productAuth);
        }
        return AbsentProductAuth==productAuth?null:productAuth;
    }
    
    public void clear(String proId) {
        getProductAuth(proId).clear();
    }
    
    public void clean() {
        productAuthCache.clear();
    }
}

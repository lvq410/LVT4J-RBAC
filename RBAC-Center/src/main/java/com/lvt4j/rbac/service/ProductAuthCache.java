package com.lvt4j.rbac.service;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvt4j.basic.TLruCache;
import com.lvt4j.rbac.ProductAuth4Center;
import com.lvt4j.rbac.data.model.Product;
import com.lvt4j.rbac.db.DataSourceConfig;
import com.lvt4j.rbac.db.DbLock;

import lombok.extern.slf4j.Slf4j;


/**
 *
 * @author LV
 */
@Slf4j
@Service
public class ProductAuthCache extends Thread {

    /** 放在缓存中,表示不存在的产品 */
    private static final ProductAuth4Center Absent = new ProductAuth4Center(null, null);
    
    @Autowired
    private Dao dao;
    
    @Autowired
    private DataSourceConfig dbConfig;
    
    @Autowired
    private DbLock dbLock;
    
    /** 所有产品缓存 */
    private TLruCache<String, ProductAuth4Center> caches = new TLruCache<String, ProductAuth4Center>(1000);
    
    @PostConstruct
    private void init() {
        if(!dbConfig.isDistributedDatabase()) return;
        //分布式类型的数据库，由于各节点均可以修改产品，因此对于产品下权限的刷新，也需要做到分布式同步，有两个方案
        //一、各节点间维持一个消息通道，有节点修改商品就互相同步
        //二、定时扫描本节点中产品权限缓存里的更新时间，与从数据库查出来的时间做比对，不等则清楚缓存。目前采用本方案
        setName("ProductAuthCache");
        start();
    }
    
    @PreDestroy
    private void destory() {
        interrupt();
    }
    
    /** 获取指定产品的权限,若产品不存在,返回null */
    public ProductAuth4Center get(String proId) {
        ProductAuth4Center productAuth = caches.get(proId);
        if(productAuth!=null) return Absent==productAuth?null:productAuth;
        synchronized (caches) {
            productAuth = caches.get(proId);
            if(productAuth!=null) return Absent==productAuth?null:productAuth;
            Product product = dao.uniqueGet(Product.class, proId);
            productAuth = product==null?Absent:new ProductAuth4Center(product, dao);
            caches.put(proId, productAuth);
            return Absent==productAuth?null:productAuth;
        }
    }
    
    public void invalidate(String proId) {
        caches.remove(proId);
    }
    
    public void clear() {
        caches.clear();
    }
    
    @Override
    public void run() {
        while(!interrupted()){
            try{
                Thread.sleep(5000);
            }catch(InterruptedException e){
                return;
            }
            try{
                for(String proId : caches.keySet()){
                    Product pro;
                    try{
                        dbLock.readLock();
                        pro = dao.uniqueGet(Product.class, proId);
                    }finally{
                        dbLock.readUnLock();
                    }
                    Long curLastModify = Optional.ofNullable(pro).map(p->p.lastModify).orElse(null);
                    Long cachedLastModity = Optional.ofNullable(caches.get(proId)).map(pa->pa.product).map(p->p.lastModify).orElse(null);
                    if(Objects.equals(curLastModify, cachedLastModity)) continue;
                    caches.remove(proId);
                    if(log.isTraceEnabled()) log.trace("产品[{}]缓存过期", proId);
                }
            }catch(Throwable e){
                log.warn("定时检查产品缓存过期异常", e);
            }
        }
    }
    
}
package com.lvt4j.rbac.service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

    @Autowired
    private Dao dao;
    
    @Autowired
    private DataSourceConfig dbConfig;
    
    @Autowired
    private DbLock dbLock;
    
    /** 所有产品缓存 */
    private LoadingCache<String, ProductAuth4Center> caches = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, ProductAuth4Center>() {
            @Override
            public ProductAuth4Center load(String proId) throws Exception {
                Product product = dao.uniqueGet(Product.class, proId);
                if(product==null) return null;
                return new ProductAuth4Center(product, dao);
            }
        }
    );
    
    @PostConstruct
    private void init() {
        if(!dbConfig.isDistributedDatabase()) return;
        //分布式类型的数据库，由于各节点均可以修改产品，因此对于产品下权限的刷新，也需要做到分布式同步，有两个方案
        //一、各节点间维持一个消息通道，有节点修改商品就互相同步
        //二、定时扫描本节点中产品权限缓存里的更新时间，与从数据库查出来的时间做比对，不等则清除缓存。目前采用本方案
        setName("ProductAuthCache");
        start();
    }
    
    @PreDestroy
    private void destory() {
        interrupt();
    }
    
    /** 获取指定产品的权限,若产品不存在,返回null */
    public ProductAuth4Center get(String proId) {
        try{
            return caches.get(proId);
        }catch(Exception e){
            return null;
        }
    }
    
    public void invalidate(String proId) {
        caches.invalidate(proId);
    }
    
    public void clear() {
        caches.invalidateAll();
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
                for(String proId : new HashSet<>(caches.asMap().keySet())){
                    Product pro;
                    try{
                        dbLock.readLock();
                        pro = dao.uniqueGet(Product.class, proId);
                    }finally{
                        dbLock.readUnLock();
                    }
                    Long curLastModify = Optional.ofNullable(pro).map(p->p.lastModify).orElse(null);
                    Long cachedLastModity = Optional.ofNullable(get(proId)).map(pa->pa.product).map(p->p.lastModify).orElse(null);
                    if(Objects.equals(curLastModify, cachedLastModity)) continue;
                    invalidate(proId);
                    if(log.isTraceEnabled()) log.trace("产品[{}]缓存过期", proId);
                }
            }catch(Throwable e){
                log.warn("定时检查产品缓存过期异常", e);
            }
        }
    }
    
}
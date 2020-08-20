package com.lvt4j.rbac;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.dao.RoleMapper;
import com.lvt4j.rbac.dao.UserMapper;
import com.lvt4j.rbac.dao.VisitorMapper;
import com.lvt4j.rbac.db.lock.DbLock;
import com.lvt4j.rbac.po.Product;
import com.lvt4j.rbac.po.User;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author LV
 */
@Slf4j
@Service
public class ProductAuthCaches {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VisitorMapper visitorMapper;
    
    @Autowired
    private DbLock dbLock;
    
    /** 所有产品缓存 */
    private LoadingCache<String, ProductAuth4Center> caches = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.DAYS)
        .removalListener(new RemovalListener<String, ProductAuth4Center>() {
            @Override @SneakyThrows
            public void onRemoval(RemovalNotification<String, ProductAuth4Center> n) {
                if(log.isTraceEnabled()) log.trace("产品[{}]缓存失效", n.getKey());
                n.getValue().close();
            }
        }).build(new CacheLoader<String, ProductAuth4Center>() {
            @Override
            public ProductAuth4Center load(String proId) throws Exception {
                try{
                    dbLock.readLock();
                    Product product = productMapper.selectOne(Product.Query.builder().id(proId).build().toWrapper());
                    if(product==null) return null;
                    return new ProductAuth4Center(product);
                }finally{
                    dbLock.readUnLock();
                }
            }
        }
    );
    
    @PreDestroy
    private void destory() {
        caches.invalidateAll();
    }
    
    /** 获取指定产品的权限,若产品不存在,返回null */
    public ProductAuth4Center get(String proId) {
        try{
            return caches.get(proId);
        }catch(Exception e){
            return null;
        }
    }
    
    public void invalidate(String proId, String userId) {
        if(log.isTraceEnabled()) log.trace("清理缓存[{},{}]", proId, userId);
        //服务端处理缓存清理通知的方式为立即清理
        try{
            if(proId!=null){ //指定产品
                ProductAuth4Center productAuth = caches.getIfPresent(proId);
                if(productAuth!=null){
                    if(userId!=null){ //指定用户,清理该产品该用户的缓存
                        productAuth.invalidate(userId);
                    }else{ //未指定用户，清理该产品所有用户的缓存
                        productAuth.invalidate(null);
                    }
                }
            }else if(userId!=null){ //未指定产品，但指定用户，清理所有产品该用户的缓存
                caches.asMap().values().forEach(c->c.invalidate(userId));
            }else{ //未指定产品与用户，清理所有产品所有用户的缓存
                caches.asMap().values().forEach(pa->pa.invalidate(null));
            }
        }catch(Throwable ig){
            log.warn("缓存[{},{}]异常", proId, userId, ig);
        }
    }
    
    
    public class ProductAuth4Center extends AbstractProductAuth implements ProductAuth4CenterMBean{
        
        private Product product;
        
        private ProductAuth4Center(Product product){
            super(product.id, new ConcurrentHashMapProductAuthCache(false));
            this.product = product;
        }
        
        public long getLastModify() {
            return product.lastModify;
        }

        @Override
        protected UserAuth loadUserAuth(String userId){
            try{
                dbLock.readLock();
                
                if(StringUtils.isBlank(userId)) return visitorAuth();
                
                UserAuth userAuth = new UserAuth();
                userAuth.userId = userId;
                
                User user = userMapper.selectOne(User.Query.builder().id(userId).build().toWrapper());
                if(user==null) return userAuth; //父类会填充游客权限
                
                userAuth.exist = true;
                userAuth.userName = user.name;
                userAuth.userDes = user.des;
                
                UserAuth visitorAuth = visitorAuth();
                
                userAuth.params = new HashMap<String, String>();
                userMapper.paramKVs(user.autoId, product.autoId).forEach(p->{
                    if(StringUtils.isEmpty(p.val)) return;
                    userAuth.params.put(p.key, p.val);
                });
                
                userAuth.roles = new LinkedHashSet<>(visitorAuth.roles);
                userAuth.accesses = new LinkedHashSet<>(visitorAuth.accesses);
                userAuth.permissions = new LinkedHashSet<>(visitorAuth.permissions);

                userMapper.roleIdAutoIds(user.autoId, product.autoId).forEach(role->{
                    if(userAuth.roles.contains(role.id)) return;
                    userAuth.roles.add(role.id);
                    userAuth.accesses.addAll(roleMapper.accessPatterns(role.autoId));
                    userAuth.permissions.addAll(roleMapper.permissionIds(role.autoId));
                });
                
                userAuth.accesses.addAll(userMapper.accessPatterns(user.autoId, product.autoId));
                userAuth.permissions.addAll(userMapper.permissionIds(user.autoId, product.autoId));
                
                return userAuth;
            }catch(Exception e){
                log.error("加载用户[{}]于产品[{}]下的权限失败!", userId, proId, e);
                return null;
            }finally {
                dbLock.readUnLock();
            }
        }
        private UserAuth visitorAuth()throws Exception{
            UserAuth visitorAuth = this.visitorAuth;
            if(visitorAuth!=null) return visitorAuth;
            synchronized(this){
                visitorAuth = this.visitorAuth;
                if(visitorAuth!=null) return visitorAuth;
                
                UserAuth va = new UserAuth();
                va.exist = false;
                
                Map<String, String> params = va.params = new HashMap<String, String>();
                visitorMapper.paramKVs(product.autoId).forEach(p->{
                    if(StringUtils.isBlank(p.val)) return;
                    params.put(p.key, p.val);
                });
                
                va.roles = new LinkedHashSet<>();
                va.accesses = new LinkedHashSet<>();
                va.permissions = new LinkedHashSet<>();
                
                visitorMapper.roleIdAutoIds(product.autoId).forEach(role->{
                    va.roles.add(role.id);
                    va.accesses.addAll(roleMapper.accessPatterns(role.autoId));
                    va.permissions.addAll(roleMapper.permissionIds(role.autoId));
                });
                
                va.accesses.addAll(visitorMapper.accessPatterns(product.autoId));
                va.permissions.addAll(visitorMapper.permissionIds(product.autoId));
                return this.visitorAuth = va;
            }
        }
        
        @Override
        public void invalidate(String userId) {
            super.invalidate(userId);
            dbLock.readLock();
            try{
                Product proNow = productMapper.selectById(product.autoId);
                if(proNow!=null) proNow = product;
            }finally{
                dbLock.readUnLock();
            }
        }
        
    }
    
    public interface ProductAuth4CenterMBean extends AbstractProductAuthMBean{
        public long getLastModify();
    }
    
}
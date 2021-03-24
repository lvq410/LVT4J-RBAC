package com.lvt4j.rbac.cluster.single;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.ProductAuthCache;
import com.lvt4j.rbac.cluster.ProductAuthCacheFactory;
import com.lvt4j.rbac.condition.DbIsSingleOnly;

import lombok.SneakyThrows;

/**
 *
 * @author LV on 2021年3月4日
 */
@Component
@Conditional(DbIsSingleOnly.class)
class ProductAuthCacheFactoryImpl implements ProductAuthCacheFactory {

    @Override @SneakyThrows
    public ProductAuthCache build(String proId) {
        return (ProductAuthCache) Class.forName("com.lvt4j.rbac.ConcurrentHashMapProductAuthCache").getConstructor(boolean.class).newInstance(false);
    }

}

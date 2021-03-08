package com.lvt4j.rbac.cluster;

import com.lvt4j.rbac.ProductAuthCache;

/**
 *
 * @author LV on 2021年3月4日
 */
public interface ProductAuthCacheFactory {

    public ProductAuthCache build(String proId);
    
}
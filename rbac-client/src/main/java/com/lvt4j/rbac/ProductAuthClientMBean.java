package com.lvt4j.rbac;

/**
 *
 * @author LV on 2020年8月10日
 */
public interface ProductAuthClientMBean extends AbstractProductAuthMBean {

    public String getId();
    
    public String getHost();
    
    public String getRbacCenter();
    
    public int getRbacCenterTimeoutInMillis();
    
}
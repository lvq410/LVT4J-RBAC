package com.lvt4j.rbac;

/**
 *
 * @author LV on 2020年8月3日
 */
public interface AbstractProductAuthMBean {
    
    /** 产品ID */
    public String getProId();
    
    /** 缓存设定容量 */
    public int getCapacity();
    /** 缓存当前容量 */
    public int getSize();
    /** 缓存命中数 */
    public long getHitCount();
    /** 缓存未命中数 */
    public long getMissCount();
    
    /** 加载成功次数 */
    public long getLoadSuccCount();
    /** 加载失败次数 */
    public long getLoadFailCount();
    
    /** 查询用户权限,若userId为空,或用户未在权限中心注册,返回游客权限 */
    public UserAuth getUserAuth(String userId);
    
    /** 立刻清理掉指定用户缓存，为null表示清理所有缓存 */
    public void invalidate(String userId);
    /** 异步重载指定用户缓存，为null表示重载已缓存的所有用户的缓存 */
    public void invalidateAsync(String userId);
    
}
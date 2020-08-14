package com.lvt4j.rbac;

import java.io.Closeable;
import java.util.function.Function;

/**
 *
 * @author LV on 2020年8月12日
 */
public abstract class ProductAuthCache implements Closeable {

    /** 权限加载器，ProductAuthCache提交给{@link ProductAuthClient}后会传入，loader.apply(userId)返回null时表示加载失败，此时不应进行缓存 */
    public Function<String, UserAuth> loader;
    final void setLoader(Function<String, UserAuth> loader) {
        this.loader = loader;
    }

    /**
     * 实现放需要满足以下逻辑：从缓存中查询，如果缓存中没有，则调用{@link #loader}加载
     * loader返回结果不为null时，结果应当缓存起来
     * @param userId
     * @param loader 
     * @return 从缓存中查询结果或loader加载结果，或null加载失败
     */
    public abstract UserAuth getOrLoad(String userId);
    
    /** 缓存设定容量 */
    public abstract int getCapacity();
    /** 缓存当前容量 */
    public abstract int getSize();
    /** 缓存命中数 */
    public abstract long getHitCount();
    /** 缓存未命中数 */
    public abstract long getMissCount();
    
    /**
     * 立刻清理掉指定用户缓存
     * @param userId 可为null表示清理所有缓存
     */
    public abstract void invalidate(String userId);
    
    /**
     * 异步重载缓存<br>
     * 支持异步重载的缓存实现需覆写该方法<br>
     * 使用{@link #loader}来加载指定用户缓存<br>
     * {@link ProductAuthClient}收到权限中心广播的缓存清理消息后，调用该方法实现缓存重载<br>
     * 如果实现为分布式缓存，且服务有多个实例，要考虑在该方法的实现中进行单例限制，因为每个ProductAuthClient实例收到清缓存通知后都会调用该方法
     * @param userId 非null表示仅重载指定用户的，null表示重载已缓存的所有用户的缓存
     */
    public void invalidateAsync(String userId){
        invalidate(userId);
    }
    
}
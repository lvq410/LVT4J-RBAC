package com.lvt4j.rbac;

import static java.util.Arrays.asList;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LV on 2020年8月12日
 */
abstract class LocalProductAuthCache extends ProductAuthCache {
    private static final Logger log = Logger.getLogger(LocalProductAuthCache.class.getName());

    private final AsyncRefresher asyncRefresher;
    
    LocalProductAuthCache(boolean supportAsyncRefresh) {
        asyncRefresher = supportAsyncRefresh?new AsyncRefresher():null;
    }
    
    public abstract boolean contains(String userId);
    public abstract Set<String> cachedUserIds();
    public abstract UserAuth put(String userId, UserAuth userAuth);
    
    @Override
    public void invalidateAsync(String userId) {
        if(asyncRefresher==null){
            super.invalidateAsync(userId);
            return;
        }
        if(log.isLoggable(FINEST)) log.finest(String.format("重载用户[%s]缓存", userId));
        if(userId==null || userId.isEmpty()){
            asyncRefresher.refresh(cachedUserIds());
        }else{
            asyncRefresher.refresh(asList(userId));
        }
    }
    
    @Override
    public void close() throws IOException {
        if(asyncRefresher!=null) asyncRefresher.destory();
    }
    
    /**
     * 缓存异步刷新器<br>
     * 持有一个待刷新用户id队列，间隔一小段时间重载缓存
     * @author LV on 2020年8月3日
     */
    class AsyncRefresher extends Thread {
        
        /** 异步刷新缓存队列 */
        private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        
        private volatile boolean destory;
        
        public AsyncRefresher() {
            setName("ProAuthRefresher");
            setDaemon(true);
            start();
        }

        public void refresh(Collection<String> userIds) {
            if(log.isLoggable(FINEST)) log.finest(String.format("入队重载缓存:%s个", userIds.size()));
            for(String userId : userIds){
                if(queue.contains(userId)) continue;
                try{
                    queue.put(userId);
                }catch(InterruptedException e){
                    log.log(Level.WARNING, String.format("入队待刷新用户%s失败", userId), e);
                }
            }
        }
        
        @Override
        public void run(){
            if(log.isLoggable(FINEST)) log.finest("缓存异步刷新器启动");
            while(!destory){
                String userId = null;
                try{
                    userId = queue.take();
                    if(log.isLoggable(FINEST)) log.finest(String.format("重载缓存:%s", userId));
                    if(!contains(userId)){
                        if(log.isLoggable(FINEST)) log.finest(String.format("未缓存，跳过重载缓存:%s", userId));
                    }else{
                        UserAuth userAuth = loader.apply(userId);
                        if(userAuth==null){
                            log.log(WARNING, String.format("刷新用户%s缓存失败", userId));
                        }else{
                            put(userId, userAuth);
                        }
                    }
                }catch(Throwable e){
                    if(destory) break;
                    log.log(WARNING, String.format("刷新用户%s缓存失败", userId), e);
                }
                try{
                    sleep(100); //刷新间隔
                }catch(Throwable ig){}
            }
            if(log.isLoggable(FINEST)) log.finest("缓存异步刷新器停止");
        }
        public void destory() {
            destory = true;
            interrupt();
            try{
                join(1000);
            }catch(InterruptedException ig){}
        }
    }
    
}
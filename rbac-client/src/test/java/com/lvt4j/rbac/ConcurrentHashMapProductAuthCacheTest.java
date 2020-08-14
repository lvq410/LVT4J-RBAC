package com.lvt4j.rbac;

import static java.util.logging.Level.ALL;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author LV on 2020年8月12日
 */
public class ConcurrentHashMapProductAuthCacheTest {

    ConcurrentHashMapProductAuthCache cache;
    
    @Before
    public void before() {
        Logger logger = Logger.getLogger(LocalProductAuthCache.class.getName());
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(ALL);
        logger.addHandler(handler);
        logger.setLevel(ALL);
    }
    
    @Test
    public void test() throws Exception {
        List<UserAuth> uas = new ArrayList<>();
        for(int i=0; i<10; i++){
            UserAuth ua = new UserAuth(); ua.userId = String.valueOf(i);
            uas.add(ua);
        }
        
        cache = new ConcurrentHashMapProductAuthCache();
        cache.loader = id->{UserAuth ua = new UserAuth();ua.userId=id;return ua;};
        
        assertEquals(0, cache.getCapacity());
        assertEquals(0, cache.getSize());
        assertEquals(0, cache.getHitCount());
        assertEquals(0, cache.getMissCount());
        
        assertEquals("0", cache.getOrLoad("0").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(1, cache.getSize());
        assertEquals(0, cache.getHitCount());
        assertEquals(1, cache.getMissCount());
        
        assertEquals("0", cache.getOrLoad("0").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(1, cache.getSize());
        assertEquals(1, cache.getHitCount());
        assertEquals(1, cache.getMissCount());
        
        assertEquals("1", cache.getOrLoad("1").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(2, cache.getSize());
        assertEquals(1, cache.getHitCount());
        assertEquals(2, cache.getMissCount());
        
        assertEquals("1", cache.getOrLoad("1").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(2, cache.getSize());
        assertEquals(2, cache.getHitCount());
        assertEquals(2, cache.getMissCount());
        
        assertEquals("3", cache.getOrLoad("3").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(3, cache.getSize());
        assertEquals(2, cache.getHitCount());
        assertEquals(3, cache.getMissCount());
        
        IntStream.range(0, 10).parallel().forEach(i->{
            IntStream.range(0, 10).forEach(j->{
                cache.getOrLoad(String.valueOf(j));
                try{ Thread.sleep(100); }catch(InterruptedException e){}
            });
        });
        assertEquals(0, cache.getCapacity());
        assertEquals(10, cache.getSize());
        assertEquals(95, cache.getHitCount());
        assertEquals(10, cache.getMissCount());
        
        cache.invalidate("0");
        assertEquals(0, cache.getCapacity());
        assertEquals(9, cache.getSize());
        assertEquals(95, cache.getHitCount());
        assertEquals(10, cache.getMissCount());
        
        assertEquals("1", cache.getOrLoad("1").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(9, cache.getSize());
        assertEquals(96, cache.getHitCount());
        assertEquals(10, cache.getMissCount());
        
        cache.loader = id->{UserAuth ua = new UserAuth();ua.userId=id+id;return ua;};
        cache.invalidateAsync("1"); Thread.sleep(500);
        assertEquals("11", cache.getOrLoad("1").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(9, cache.getSize());
        assertEquals(97, cache.getHitCount());
        assertEquals(10, cache.getMissCount());
        
        assertEquals("2", cache.getOrLoad("2").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(9, cache.getSize());
        assertEquals(98, cache.getHitCount());
        assertEquals(10, cache.getMissCount());
        
        cache.invalidateAsync("0"); Thread.sleep(500);
        assertEquals(0, cache.getCapacity());
        assertEquals(9, cache.getSize());
        assertEquals(98, cache.getHitCount());
        assertEquals(10, cache.getMissCount());
        
        cache.invalidateAsync(null); Thread.sleep(1500);
        assertEquals("22", cache.getOrLoad("2").userId);
        assertEquals("99", cache.getOrLoad("9").userId);
        assertEquals(0, cache.getCapacity());
        assertEquals(9, cache.getSize());
        assertEquals(100, cache.getHitCount());
        assertEquals(10, cache.getMissCount());
        
        cache.close();
    }
    
}

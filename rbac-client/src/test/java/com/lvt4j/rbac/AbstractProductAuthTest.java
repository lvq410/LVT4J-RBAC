package com.lvt4j.rbac;

import static java.util.Arrays.asList;
import static java.util.logging.Level.ALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 *
 * @author LV on 2020年8月4日
 */
public class AbstractProductAuthTest {

    private UserAuth visitorAuth;
    private UserAuth va2load;
    
    private JunitProAuth proAuth;
    
    @Before
    public void before() throws Exception {
        Logger log = Logger.getLogger(AbstractProductAuth.class.getName());
        Handler handler = new ConsoleHandler();
        handler.setLevel(ALL);
        log.addHandler(handler);
        log.setLevel(Level.FINEST);
        
        visitorAuth = new UserAuth();
        visitorAuth.params = new HashMap<String, String>();
        visitorAuth.params.put("visitor", "visitor");
        visitorAuth.roles = new HashSet<String>(Arrays.asList("visitor"));
        visitorAuth.accesses = new HashSet<String>(Arrays.asList("visitor"));
        visitorAuth.permissions = new HashSet<String>(Arrays.asList("visitor"));
        
        proAuth = new JunitProAuth("junit");
    }
    
    @After
    public void after() throws Exception {
        proAuth.close();
    }
    
    @Test
    public void test() throws Exception {
        assertEquals(0, proAuth.getCapacity());
        assertEquals(0, proAuth.getSize());
        assertEquals(0, proAuth.getHitCount());
        assertEquals(0, proAuth.getMissCount());
        assertEquals(0, proAuth.getLoadSuccCount());
        assertEquals(0, proAuth.getLoadFailCount());
        
        //用户存在且加载成功
        UserAuth ua = proAuth.getUserAuth("1");
        assertUserAuth(ua, "1");
        assertEquals(1, proAuth.getSize());
        assertEquals(0, proAuth.getHitCount());
        assertEquals(1, proAuth.getMissCount());
        assertEquals(1, proAuth.getLoadSuccCount());
        assertEquals(0, proAuth.getLoadFailCount());
        
        ua = proAuth.getUserAuth("1");
        assertUserAuth(ua, "1");
        assertEquals(1, proAuth.getSize());
        assertEquals(1, proAuth.getHitCount());
        assertEquals(1, proAuth.getMissCount());
        assertEquals(1, proAuth.getLoadSuccCount());
        assertEquals(0, proAuth.getLoadFailCount());
        
        //用户不存在但加载成功
        //应当填充游客权限，但游客权限加载失败，填充为空权限
        ua = proAuth.getUserAuth("10");
        assertUserAuthNull(ua, "10");
        assertEquals(1, proAuth.getSize()); //已缓存数量不变，因为游客权限加载失败
        assertEquals(1, proAuth.getHitCount());
        assertEquals(2, proAuth.getMissCount());
        assertEquals(2, proAuth.getLoadSuccCount());
        assertEquals(2, proAuth.getLoadFailCount()); //加载游客失败二次
        
        //用户不存在但加载成功
        //应当且成功填充游客权限
        va2load = visitorAuth;
        ua = proAuth.getUserAuth("10");
        assertUserAuthVisitor(ua, "10");
        assertEquals(2, proAuth.getSize()); //已缓存数量+1
        assertEquals(1, proAuth.getHitCount());
        assertEquals(3, proAuth.getMissCount()); //游客不在缓存中
        assertEquals(4, proAuth.getLoadSuccCount());
        assertEquals(2, proAuth.getLoadFailCount());
        
        //用户加载失败
        //应当填充游客权限，但游客权限加载失败，填充为空权限
        va2load = null;
        proAuth.invalidate(null);
        ua = proAuth.getUserAuth("20");
        assertUserAuthNull(ua, "20");
        assertEquals(0, proAuth.getSize()); //已缓存数量不变（被刚清零）
        assertEquals(1, proAuth.getHitCount());
        assertEquals(4, proAuth.getMissCount());
        assertEquals(4, proAuth.getLoadSuccCount());
        assertEquals(4, proAuth.getLoadFailCount());
        
        //用户加载失败
        //应当且成功填充游客权限
        va2load = visitorAuth;
        ua = proAuth.getUserAuth("20");
        assertUserAuthVisitor(ua, "20");
        assertEquals(0, proAuth.getSize()); //已缓存数量不变，因加载失败
        assertEquals(1, proAuth.getHitCount());
        assertEquals(5, proAuth.getMissCount());
        assertEquals(5, proAuth.getLoadSuccCount()); //游客加载成功一次
        assertEquals(5, proAuth.getLoadFailCount());
    }
    
    private void assertUserAuth(UserAuth ua, String idx) {
        assertEquals(idx, ua.userId);
        assertEquals(idx, ua.userName);
        assertEquals(idx, ua.userDes);
        assertEquals(ImmutableMap.of(idx,idx), ua.params);
        assertEquals(ImmutableSet.of(idx), ua.roles);
        assertEquals(ImmutableSet.of(idx), ua.accesses);
        assertEquals(ImmutableSet.of(idx), ua.permissions);
    }
    private void assertUserAuthVisitor(UserAuth ua, String idx) {
        assertEquals(idx, ua.userId);
        assertNull(ua.userName);
        assertNull(ua.userDes);
        assertEquals(ImmutableMap.of("visitor","visitor"), ua.params);
        assertEquals(ImmutableSet.of("visitor"), ua.roles);
        assertEquals(ImmutableSet.of("visitor"), ua.accesses);
        assertEquals(ImmutableSet.of("visitor"), ua.permissions);
    }
    private void assertUserAuthNull(UserAuth ua, String idx) {
        assertEquals(idx, ua.userId);
        assertNull(ua.userName);
        assertNull(ua.userDes);
        assertEquals(ImmutableMap.of(), ua.params);
        assertEquals(ImmutableSet.of(), ua.roles);
        assertEquals(ImmutableSet.of(), ua.accesses);
        assertEquals(ImmutableSet.of(), ua.permissions);
    }
    
    class JunitProAuth extends AbstractProductAuth {
        
        private boolean doubleId;
        
        public JunitProAuth(String proId) {
            super(proId, new ConcurrentHashMapProductAuthCache());
        }
        @Override
        protected UserAuth loadUserAuth(String userId) {
            if(userId==null) return va2load;
            int i = Integer.valueOf(userId);
            if(i<10) { //代表存在并加载成功
                UserAuth auth = new UserAuth();
                String idx = auth.userId = auth.userName = auth.userDes = String.valueOf(i);
                if(doubleId) auth.userId = idx+idx;
                auth.exist = true;
                auth.params = new HashMap<String, String>();
                auth.params.put(idx, idx);
                auth.roles = new HashSet<String>(asList(idx));
                auth.accesses = new HashSet<String>(asList(idx));
                auth.permissions = new HashSet<String>(asList(idx));
                return auth;
            }
            if(i<20) { //代表不存在但加载成功
                UserAuth ua = new UserAuth();
                ua.userId = userId;
                return ua;
            }
            return null; //代表加载失败
        }
        
    }
    
}

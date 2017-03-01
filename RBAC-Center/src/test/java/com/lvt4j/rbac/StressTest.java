package com.lvt4j.rbac;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.lvt4j.rbac.data.bean.Product;
import com.lvt4j.rbac.web.controller.EditController;


/**
 * 压力测试
 * @author lichenxi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes={Main.class})
@WebAppConfiguration
@FixMethodOrder
public class StressTest {
    
    @Autowired
    EditController editController;
    
    /** 创建1W个产品 */
    @Test
    public void cPros() {
        long begin = System.currentTimeMillis();
        Product pro = new Product();
        for (int i = 0; i < 10000; i++) {
            pro.id = "pro"+i;
            pro.name = pro.id;
            pro.des = pro.id;
            editController.productSet(null, pro);
        }
        long load = System.currentTimeMillis()-begin;
        System.out.println("cPros:"+load+"("+(load/10000)+")");
    }
    /** 创建100W个用户 */
    @Test
    public void cUsers() {
        
        
        
    }
}

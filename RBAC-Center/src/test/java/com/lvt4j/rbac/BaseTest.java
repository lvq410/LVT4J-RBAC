package com.lvt4j.rbac;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


/**
 * Junit测试继承此类
 * @author LV
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes={Main.class})
@WebAppConfiguration
@FixMethodOrder
public class BaseTest {
    
}

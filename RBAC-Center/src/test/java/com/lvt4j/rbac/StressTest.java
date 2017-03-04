package com.lvt4j.rbac;

import java.util.LinkedList;
import java.util.List;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.basic.TScan;
import com.lvt4j.rbac.data.bean.Access;
import com.lvt4j.rbac.data.bean.Param;
import com.lvt4j.rbac.data.bean.Permission;
import com.lvt4j.rbac.data.bean.Product;
import com.lvt4j.rbac.data.bean.Role;
import com.lvt4j.rbac.data.bean.User;
import com.lvt4j.rbac.web.controller.EditController;


/**
 * 压力测试
 * @author LV
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes={Main.class})
@WebAppConfiguration
@FixMethodOrder
public class StressTest {
    
    @Autowired
    TDB db;
    
    @Autowired
    EditController editController;
    
    List<Integer> proList;
    List<Integer> userList;
    
    @Before
    public void clean() throws Exception {
        for (Class<?> cls : TScan.scanClass("com.lvt4j.rbac.data.bean")) {
            Table tbl = cls.getAnnotation(Table.class);
            if(tbl==null) continue;
            db.executeSQL("delete from "+tbl.value()).execute();
        }
        proList = new LinkedList<Integer>();
        for (int i = 0; i < 1000; i++) proList.add(i);
        userList = new LinkedList<Integer>();
        for (int i = 0; i < 10000; i++) userList.add(i);
    }
    
    //创建1000个产品
    @Test
    public void cPros() {
        System.out.println("cPros");
        for(Integer i : proList) {
            Product pro = new Product();
            pro.id = "pro"+i;
            pro.name = pro.id;
            pro.des = pro.id;
            editController.productSet(null, pro);
        }
        db.endTransaction();
    }
    
    //创建1W个用户
    @Test
    public void cUsers() {
        System.out.println("cUsers");
        for(Integer i : userList) {
            User user = new User();
            user.id = "user"+i;
            user.name = user.id;
            user.des = user.id;
            editController.userSet(null, user);
        }
        db.endTransaction();
    }
    
    //每个产品创建50个配置项,500个访问项,500个授权,50个角色(角色拥有前100个访问项和授权项)
    @Test
    public void cParams() {
        System.out.println("cParams...");
        for(Integer i : proList) {
            Param param = new Param();
            param.proId = "pro"+i;
            for (int j = 0; j < 50; j++) {
                param.key = "param"+j;
                param.name = param.key;
                param.des = param.key;
                editController.paramSet(null, param);
            }
            Access access = new Access();
            access.proId = "pro"+i;
            Permission permission = new Permission();
            permission.proId = "pro"+i;
            for (int j = 0; j < 500; j++) {
                access.pattern = "^/"+j+"$";
                access.name = access.pattern;
                access.des = access.pattern;
                editController.accessSet(null, access);
                permission.id = "permission"+j;
                permission.name = permission.id;
                permission.des = permission.id;
                editController.permissionSet(null, permission);
            }
            Role role = new Role();
            role.proId = "pro"+i;
            String[] accessPatterns = new String[100];
            String[] permissionIds = new String[100];
            for (int k = 0; k < 100; k++) {
                accessPatterns[k] = "^/"+k+"$";
                permissionIds[k] = "permission"+k;
            }
            for (int j = 0; j < 50; j++) {
                role.id = "role"+j;
                role.name = role.id;
                role.des = role.id;
                editController.roleSet(null, role, accessPatterns, permissionIds);
            }
        }
        db.endTransaction();
    }
    
    //为每个游客设置配置项、分配第0个角色、第200个访问项、第200个授权项
    @Test
    public void cVisitors() {
        System.out.println("cVisitors...");
        JSONObject params = new JSONObject();
        for (int j = 0; j < 50; j++) params.put("param"+j, "param_visitor"+j);
        for(Integer i : proList) {
            String[] roleIds = {"role0"};
            String[] accessPatterns = {"^/200$"};
            String[] permissionIds = {"permission200"};
            editController.authVisitorSet("pro"+i, params, roleIds, accessPatterns, permissionIds);
        }
        db.endTransaction();
    }
    
    //为前100个产品前100个用户设置配置项、分配第10~20个角色、第300~400个访问项、第300~400个授权项
    @Test
    public void cUserAuths() throws Exception {
        System.out.println("cUsers...");
        JSONObject params = new JSONObject();
        for (int j = 0; j < 50; j++) params.put("param"+j, "param_user"+j);
        String[] roleIds = new String[10];
        for (int j = 10; j < 20; j++) roleIds[j-10] = "role"+j;
        String[] accessPatterns = new String[100];
        for (int j = 300; j < 400; j++) accessPatterns[j-300] = "^/"+j+"$";
        String[] permissionIds = new String[100];
        for (int j = 300; j < 400; j++) permissionIds[j-300] = "permission"+j;
        for (int i = 0; i < 100; i++) {
            String userId = "user"+i;
            for (int proI = 0; proI < 100; proI++) {
                String proId = "pro"+proI;
                editController.authUserSet(proId, userId, params, roleIds, accessPatterns, permissionIds);
            }
        }
        db.endTransaction();
    }
    
}

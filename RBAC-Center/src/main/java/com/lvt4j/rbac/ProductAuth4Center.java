package com.lvt4j.rbac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TVerify;
import com.lvt4j.rbac.data.model.Access;
import com.lvt4j.rbac.data.model.Param;
import com.lvt4j.rbac.data.model.Permission;
import com.lvt4j.rbac.data.model.Product;
import com.lvt4j.rbac.data.model.Role;
import com.lvt4j.rbac.data.model.User;
import com.lvt4j.rbac.service.Dao;
import com.lvt4j.rbac.service.Dao.AuthCalRst;

@Slf4j
public class ProductAuth4Center extends AbstractProductAuth{

    TDB db = Consts.DB;
    
    Dao dao = Consts.Dao;

    public Product product;
    
    public ProductAuth4Center(Product product){
        super(product==null?null:product.id, 5000);
        this.product = product;
    }

    @Override
    protected UserAuth loadUserAuth(String userId){
        try{
            if(TVerify.strNullOrEmpty(userId)) return loadVisitorAuth();
            UserAuth userAuth = new UserAuth();
            userAuth.userId = userId;
            
            User user = dao.uniqueGet(User.class, userId);
            userAuth.exist = user!=null;
            if(!userAuth.exist) {
                userAuth.params = loadVisitorAuth().params;
                userAuth.roles = loadVisitorAuth().roles;
                userAuth.accesses = loadVisitorAuth().accesses;
                userAuth.permissions = loadVisitorAuth().permissions;
                return userAuth;
            }
            
            Map<String, String> param = new HashMap<String, String>();
            List<Param> rawParams = dao.params("user", product.autoId, user.autoId);
            for(Param rawParam : rawParams){
                if(StringUtils.isEmpty(rawParam.val)) continue;
                param.put(rawParam.key, rawParam.val);
            }
            userAuth.params = param;
            
            AuthCalRst authCalRst = dao.authCal(product.autoId, user.autoId);
            
            userAuth.roles = new HashSet<String>();
            List<Role> roles = authCalRst.getAuths(Role.class);
            for(Role role : roles) userAuth.roles.add(role.id);
            
            userAuth.accesses = new HashSet<String>();
            List<Access> accesses = authCalRst.getAuths(Access.class);
            for(Access access : accesses) userAuth.accesses.add(access.pattern);
            
            userAuth.permissions = new HashSet<String>();
            List<Permission> permissions = authCalRst.getAuths(Permission.class);
            for(Permission permission : permissions) userAuth.permissions.add(permission.id);
            
            return userAuth;
        }catch(Exception e){
            log.error("加载用户[{}]于产品[{}]下的权限失败!",
                    new Object[]{userId, proId, e});
            return null;
        }
    }

    private UserAuth loadVisitorAuth()throws Exception{
        UserAuth visitorAuth = this.visitorAuth;
        if(visitorAuth!=null) return visitorAuth;
        synchronized(this){
            visitorAuth = this.visitorAuth;
            if(visitorAuth!=null) return visitorAuth;
            
            visitorAuth = new UserAuth();
            visitorAuth.exist = false;
            
            AuthCalRst authCalRst = dao.authCal(product.autoId, null);
            
            visitorAuth.roles = new HashSet<String>();
            List<Role> roles = authCalRst.getAuths(Role.class);
            for(Role role : roles) visitorAuth.roles.add(role.id);
            
            visitorAuth.accesses = new HashSet<String>();
            List<Access> accesses = authCalRst.getAuths(Access.class);
            for(Access access : accesses) visitorAuth.accesses.add(access.pattern);
            
            visitorAuth.permissions = new HashSet<String>();
            List<Permission> permissions = authCalRst.getAuths(Permission.class);
            for(Permission permission : permissions) visitorAuth.permissions.add(permission.id);
            
            this.visitorAuth = visitorAuth;
            return visitorAuth;
        }
    }
    
}

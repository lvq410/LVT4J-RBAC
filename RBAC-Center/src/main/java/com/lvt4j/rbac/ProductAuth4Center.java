package com.lvt4j.rbac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TVerify;
import com.lvt4j.rbac.data.bean.Product;
import com.lvt4j.rbac.data.bean.User;
import com.lvt4j.rbac.data.bean.UserParam;
import com.lvt4j.rbac.data.bean.VisitorParam;

public class ProductAuth4Center extends AbstractProductAuth{

    TDB db = Consts.DB;

    public Product product;
    
    public ProductAuth4Center(Product product){
        super(product==null?null:product.id,
                product==null?0:5000);
        this.product = product;
    }

    @Override
    protected UserAuth loadUserAuth(String userId){
        if(TVerify.strNullOrEmpty(userId)) return loadVisitorAuth();
        UserAuth userAuth = new UserAuth();
        userAuth.userId = userId;
        
        userAuth.exist = db.exist(User.class, userId).execute();
        if(!userAuth.exist) {
            userAuth.params = loadVisitorAuth().params;
            userAuth.roles = loadVisitorAuth().roles;
            userAuth.accesses = loadVisitorAuth().accesses;
            userAuth.permissions = loadVisitorAuth().permissions;
            return userAuth;
        }
        
        Map<String, String> param = null;
        List<UserParam> rawParams = db.select("select key,val from user_param where proId=? and userId=?",
                proId, userId).execute2Model(UserParam.class);
        param = new HashMap<String, String>(rawParams.size());
        for(UserParam rawParam : rawParams) param.put(rawParam.key, rawParam.val);
        userAuth.params = param;
        
        userAuth.roles = new HashSet<String>(db.select(
                "select roleId from user_role where proId=? and userId=? "
                + "union "
                + "select roleId from visitor_role where proId=?",
                proId, userId, proId).execute2Basic(String.class));
        
        userAuth.accesses = new HashSet<String>(db.select(
                "select accessPattern from role_access where proId=? and roleId in(" //所有角色拥有的访问项
                    +"select roleId from user_role where proId=? and userId=? " //用户拥有的角色
                    + "union "
                    + "select roleId from visitor_role where proId=?" //联合游客拥有的角色
                +") "
                +"union "
                +"select accessPattern from user_access where proId=? and userId=?" //联合单独分配给用户的访问项
                +"union "
                +"select accessPattern from visitor_access where proId=?", // 联合分配给游客的访问项
                proId, proId, userId, proId, proId, userId, proId).execute2Basic(String.class));
        
        userAuth.permissions = new HashSet<String>(db.select(
                "select permissionId from role_permission where proId=? and roleId in(" //所有角色拥有的授权项
                    +"select roleId from user_role where proId=? and userId=? " //用户拥有的角色
                    +"union "
                    +"select roleId from visitor_role where proId=?" //联合游客拥有的角色
                +")"
                +"union "
                +"select permissionId from user_permission where proId=? and userId=?" //联合单独分配给用户的授权项
                +"union "
                +"select permissionId from visitor_permission where proId=?", //联合分配给游客的授权项
                proId, proId, userId, proId, proId, userId).execute2Basic(String.class));
        
        return userAuth;
    }

    private UserAuth loadVisitorAuth() {
        UserAuth visitorAuth = this.visitorAuth;
        if(visitorAuth!=null) return visitorAuth;
        visitorAuth = new UserAuth();
        visitorAuth.exist = false;
        
        Map<String, String> param = null;
        List<VisitorParam> rawParams = db.select("select key,val from visitor_param where proId=?",
                proId).execute2Model(VisitorParam.class);
        param = new HashMap<String, String>(rawParams.size());
        for(VisitorParam rawParam : rawParams) param.put(rawParam.key, rawParam.val);
        visitorAuth.params = param;
        
        visitorAuth.roles = new HashSet<String>(db.select("select roleId from visitor_role where proId=?",
                proId).execute2Basic(String.class));
        
        visitorAuth.accesses = new HashSet<String>(db.select(
                "select accessPattern from role_access where proId=? and roleId in(" //所有角色拥有的访问项
                    +"select roleId from visitor_role where proId=?" //游客拥有的角色
                +") "
                +"union "
                +"select accessPattern from visitor_access where proId=?", // 联合分配给游客的访问项
                 proId, proId, proId).execute2Basic(String.class));
        
        visitorAuth.permissions = new HashSet<String>(db.select(
                "select permissionId from role_permission where proId=? and roleId in(" //所有角色拥有的授权项
                    +"select roleId from visitor_role where proId=?" //游客拥有的角色
                +")"
                +"union "
                +"select permissionId from visitor_permission where proId=?", //联合分配给游客的授权项
                proId, proId, proId).execute2Basic(String.class));
        
        return visitorAuth;
    }
    
}

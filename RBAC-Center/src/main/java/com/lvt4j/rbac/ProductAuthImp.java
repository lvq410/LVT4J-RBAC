package com.lvt4j.rbac;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvt4j.rbac.AbstractProductAuth;
import com.lvt4j.rbac.UserAuth;
import com.lvt4j.rbac.data.bean.Product;
import com.lvt4j.rbac.data.bean.UserParam;
import com.lvt4j.rbac.data.bean.base.BaseParam;

public class ProductAuthImp extends AbstractProductAuth{

    public Product product;
    
    public ProductAuthImp(Product product){
        super(product==null?"":product.id,
                product==null?1:5000);
        this.product = product;
    }

    @Override
    protected UserAuth loadUserAuth(String userId){
        UserAuth userAuth = new UserAuth();
        userAuth.userId = userId;
        userAuth.exist = Consts.DB.select("select count(id)<>0 from user where id=?",
                userId).execute2BasicOne(boolean.class);
        if(!userAuth.exist) return userAuth;
        
//        Map<String, String> param = new HashMap<String, String>();
//        List<UserParam> rawParams = Consts.DB.select("select key,val from user_param where proId=? and userId=?",
//                proId, userId).execute2Model(UserParam.class);
//        for(UserParam rawParam : rawParams) rawParam. param.put(rawParam.key, rawParam.val);
//        userAuth.param = param;
//        
//        List<String> roleIds = Consts.DB.select("select roleId rom user_role where proId=? and userId=?",
//                proId, userId).execute2Basic(String.class);
        
//        Set<String> access = new HashSet<String>();
//        List<String> rawAccess = Consts.DB.select(
//                "select accessPattern from user_access where proId=? and userId=?",
//                proId, userId).execute2Basic(String.class);
//        access.addAll(rawAccess);
//        for(String roleId : roleIds)
//            access.addAll(Consts.DB.select(
//                    "select accessPattern from role_access where proId=? and roleId=?",
//                    proId, roleId).execute2Basic(String.class));
//        userAuth.access = access;
//        
//        Set<String> permission = new HashSet<String>();
//        List<String> rawPermission = Consts.DB.select(
//                "select permissionId from user_permission where proId=? and userId=?",
//                proId, userId).execute2Basic(String.class);
//        permission.addAll(rawPermission);
//        for(String roleId : roleIds)
//            permission.addAll(Consts.DB.select(
//                    "select permissionId from role_permission where proId=? and roleId=?",
//                    proId, roleId).execute2Basic(String.class));
//        userAuth.permission = permission;
        
        return userAuth;
    }

}

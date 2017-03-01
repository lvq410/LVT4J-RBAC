package com.lvt4j.rbac;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TVerify;
import com.lvt4j.rbac.data.bean.Product;
import com.lvt4j.rbac.data.bean.base.BaseParam;

public class ProductAuthImp extends AbstractProductAuth{

    TDB db = Consts.DB;

    public Product product;
    
    UserAuth visitorAuth;
    
    public ProductAuthImp(Product product){
        super(product==null?null:product.id,
                product==null?0:5000);
        this.product = product;
    }

    @Override
    protected UserAuth loadUserAuth(String userId){
        if(TVerify.strNullOrEmpty(userId)) return loadVisitorAuth();
        UserAuth userAuth = new UserAuth();
        userAuth.userId = userId;
        
        userAuth.exist = db.select("select count(id)<>0 from user where id=?",
                userId).execute2BasicOne(boolean.class);
        if(!userAuth.exist) {
            userAuth.param = loadVisitorAuth().param;
            userAuth.roles = loadVisitorAuth().roles;
            userAuth.access = loadVisitorAuth().access;
            userAuth.permission = loadVisitorAuth().permission;
            return userAuth;
        }
        
        Map<String, String> param = null;
        List<BaseParam> rawParams = db.select("select key,val from user_param where proId=? and userId=?",
                proId, userId).execute2Model(BaseParam.class);
        param = new HashMap<String, String>(rawParams.size());
        for(BaseParam rawParam : rawParams) param.put(rawParam.key, rawParam.val);
        userAuth.param = param;
        
        userAuth.roles = new HashSet<String>(db.select(
                "select roleId from ("
                    + "select roleId from user_role where proId=? and userId=? "
                    + "union "
                    + "select roleId from visitor_role where proId=?"
                + ")", proId, userId).execute2Basic(String.class));
        
        userAuth.access = new HashSet<String>(db.select(
                "select distinct accessPattern from (" //所有访问项去重
                    +"select accessPattern from role_access where proId=? and roleId in(" //所有角色拥有的访问项
                        +"select distinct roleId from (" //所有角色去重
                            +"select roleId from user_role where proId=? and userId=? " //用户拥有的角色
                            + "union "
                            + "select roleId from visitor_role where proId=?" //联合游客拥有的角色
                        +")"
                    +") "
                    +"union "
                    +"select accessPattern from user_access where proId=? and userId=?" //联合单独分配给用户的访问项
                    +"union "
                    +"select accessPattern from visitor_access where proId=?" // 联合分配给游客的访问项
                +")", proId, proId, userId, proId, proId, userId, proId).execute2Basic(String.class));
        
        userAuth.permission = new HashSet<String>(db.select(
                "select distinct permissionId from (" //所有授权项去重
                    +"select permissionId from role_permission where proId=? and roleId in(" //所有角色拥有的授权项
                        +"select distinct roleId from (" //
                            +"select roleId from user_role where proId=? and userId=? " //用户拥有的角色
                            +"union "
                            +"select roleId from visitor_role where proId=?" //联合游客拥有的角色
                        +")"
                    +")"
                    +"union "
                    +"select permissionId from user_permission where proId=? and userId=?" //联合单独分配给用户的授权项
                    +"union "
                    +"select permissionId from visitor_permission where proId=?" //联合分配给游客的授权项
                +")", proId, proId, userId, proId, proId, userId).execute2Basic(String.class));
        
        return userAuth;
    }

    private UserAuth loadVisitorAuth() {
        if(visitorAuth!=null) return visitorAuth;
        visitorAuth = new UserAuth();
        visitorAuth.exist = true;
        
        Map<String, String> param = null;
        List<BaseParam> rawParams = db.select("select key,val from visitor_param where proId=?",
                proId).execute2Model(BaseParam.class);
        param = new HashMap<String, String>(rawParams.size());
        for(BaseParam rawParam : rawParams) param.put(rawParam.key, rawParam.val);
        visitorAuth.param = param;
        
        visitorAuth.roles = new HashSet<String>(db.select("select roleId from visitor_role where proId=?",
                proId).execute2Basic(String.class));
        
        visitorAuth.access = new HashSet<String>(db.select(
                "select distinct accessPattern from (" //所有访问项去重
                    +"select accessPattern from role_access where proId=? and roleId in(" //所有角色拥有的访问项
                        +"select roleId from visitor_role where proId=?" //游客拥有的角色
                    +") "
                    +"union "
                    +"select accessPattern from visitor_access where proId=?" // 联合分配给游客的访问项
                +")", proId, proId, proId).execute2Basic(String.class));
        
        visitorAuth.permission = new HashSet<String>(db.select(
                "select distinct permissionId from (" //所有授权项去重
                    +"select permissionId from role_permission where proId=? and roleId in(" //所有角色拥有的授权项
                        +"select roleId from visitor_role where proId=?" //游客拥有的角色
                    +")"
                    +"union "
                    +"select permissionId from visitor_permission where proId=?" //联合分配给游客的授权项
                +")", proId, proId, proId).execute2Basic(String.class));
        
        return visitorAuth;
    }
    
    public byte[] userAuthSerialize(String userId) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(getUserAuth(userId));
        return baos.toByteArray();
    }
    
}

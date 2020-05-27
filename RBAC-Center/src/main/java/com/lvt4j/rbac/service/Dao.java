package com.lvt4j.rbac.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.basic.TPager;
import com.lvt4j.rbac.Consts.ErrCode;
import com.lvt4j.rbac.data.Model;
import com.lvt4j.rbac.data.model.Access;
import com.lvt4j.rbac.data.model.OpLog;
import com.lvt4j.rbac.data.model.Param;
import com.lvt4j.rbac.data.model.Permission;
import com.lvt4j.rbac.data.model.Product;
import com.lvt4j.rbac.data.model.Role;
import com.lvt4j.rbac.data.model.User;
import com.lvt4j.rbac.data.model.UserParam;
import com.lvt4j.spring.Err;

import lombok.Data;
import net.sf.json.JSONObject;

/**
 * @author LV
 */
@Service
public class Dao{

    @Autowired
    private TDB db;
    
    @Autowired
    @Lazy
    private ProductAuthCache productAuthCache;
    
    public Pair<Long, List<? extends Model>> list(String modelName, Integer proAutoId,
            Integer roleAutoId, Integer accessAutoId, Integer permissionAutoId,
            String keyword, TPager pager){
        Class<? extends Model> modelCls = Model.getModelCls(modelName);
        StringBuilder sql = new StringBuilder("select * from ").append(modelName);
        StringBuilder whereClause = new StringBuilder(" where seq>=0 ");
        
        List<Object> args = new LinkedList<Object>();
        if(proAutoId!=null && User.class!=modelCls && Product.class!=modelCls){
            whereClause.append("and proAutoId=? ");
            args.add(proAutoId);
        }
        if(roleAutoId!=null){
            whereClause.append("and autoId in (select "+modelName+"AutoId from "+modelName+"_role where roleAutoId=?)");
            args.add(roleAutoId);
        }
        if(accessAutoId!=null){
            whereClause.append("and autoId in (select "+modelName+"AutoId from "+modelName+"_access where accessAutoId=?)");
            args.add(accessAutoId);
        }
        if(permissionAutoId!=null){
            whereClause.append("and autoId in (select "+modelName+"AutoId from "+modelName+"_permission where permissionAutoId=?)");
            args.add(permissionAutoId);
        }
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            whereClause.append("and (");
            boolean first = true;
            for(Field field : Model.getLikeFields(modelCls)){
                if(!first) whereClause.append("or ");
                whereClause.append(field.getName()).append(" like ? ");
                args.add(keyword);
                first = false;
            }
            whereClause.append(")");
        }
        sql.append(whereClause);
        
        StringBuilder countSql = new StringBuilder("select count(autoId) from ").append(modelName).append(whereClause);
        long count = db.select(countSql.toString(), args.toArray()).execute2BasicOne(long.class);
        
        sql.append("order by seq ");
        if(pager!=null){
            sql.append("limit ?,?");
            args.add(pager.getStart());
            args.add(pager.getSize());
        }
        List<? extends Model> models = db.select(sql.toString(), args.toArray()).execute2Model(modelCls);
        return Pair.of(count, models);
    }
    public <E> E get(Class<E> modelCls, Integer autoId){
        if(autoId==null) return null;
        return db.get(modelCls, autoId).execute();
    }
    /**
     * 用unique索引,判断一个基本Model是否冲突<br>
     * 若是修改(autoId有值)，且与存储的一致，则不冲突<br>
     * 否则若根据unique索引能找到,则冲突<br>
     * @return 是否冲突与原Model
     */
    public Pair<Boolean, Model> isDuplicated(Model model)throws Exception{
        Class<? extends Model> modelCls = model.getClass();
        Model origModel = get(modelCls, (Integer)model.get("autoId"));
        if(origModel!=null){
            boolean equalOld = true;
            for(Field field : Model.getUniqueFields(modelCls)){
                if(field.get(origModel).equals(field.get(model))) continue;
                equalOld = false;
                break;
            }
            if(equalOld) return Pair.of(false, origModel);
        }
        
        StringBuilder sql = new StringBuilder("select count(*)<>0 from ")
            .append(modelCls.getAnnotation(Table.class).value()).append(" where ");
        List<Object> args = new LinkedList<Object>();
        boolean first = true;
        for (Field field : Model.getUniqueFields(modelCls)){
            if(!first) sql.append("and ");
            sql.append(field.getName()).append("=? ");
            args.add(field.get(model));
            first = false;
        }
        boolean duplicated = db.select(sql.toString(), args.toArray()).execute2BasicOne(boolean.class);
        return Pair.of(duplicated, origModel);
    }
    public <E extends Model> E uniqueGet(Class<E> modelCls, Object... uniqueVals){
        StringBuilder sql = new StringBuilder("select * from ")
            .append(modelCls.getAnnotation(Table.class).value()).append(" where ");
        List<Object> args = new LinkedList<Object>();
        int i=0;
        for (Field field : Model.getUniqueFields(modelCls)){
            if(i!=0) sql.append("and ");
            sql.append(field.getName()).append("=? ");
            args.add(uniqueVals[i++]);
        }
        return db.select(sql.toString(), args.toArray()).execute2ModelOne(modelCls);
    }
    public void set(Model model)throws Exception{
        Integer autoId = (Integer)model.get("autoId");
        if(autoId==null){
            db.insert(model).execute();
            autoId = (Integer)model.get("autoId");
            model.set("seq", autoId);
        }
        db.update(model).execute();
        if(model instanceof Product){
            productNotify(autoId);
            return;
        }
        Integer proAutoId = (Integer)model.get("proAutoId");
        if(proAutoId!=null) productNotify(proAutoId);
        else productNotify();
    }
    public void sort(String modelName, int[] autoIds)throws Exception{
        if(ArrayUtils.isEmpty(autoIds)) return;
        Class<? extends Model> modelCls = Model.getModelCls(modelName);
        List<Integer> seqs = new ArrayList<Integer>(autoIds.length);
        for(int autoId : autoIds){
            Model model = get(modelCls, autoId);
            if(model==null) throw new Err(ErrCode.NotFound);
            seqs.add((Integer)model.get("seq"));
        }
        Collections.sort(seqs);
        for(int i=0; i<autoIds.length; i++)
            db.executeSQL("update "+modelName+" set seq=? where autoId=?",
                    seqs.get(i), autoIds[i]).execute();
    }
    public void del(Class<? extends Model> modelCls, int autoId)throws Exception{
        Model model = get(modelCls, autoId);
        if(model==null) return;
        if(User.class==modelCls){
            productNotify(db.select(
                    "select distinct proAutoId from user_param where userAutoId=? "
                    +"union select distinct proAutoId from user_role where userAutoId=? "
                    +"union select distinct proAutoId from user_access where userAutoId=? "
                    +"union select distinct proAutoId from user_permission where userAutoId=?",
                    autoId, autoId, autoId, autoId).execute2Basic(Integer.class)
                    .toArray(new Integer[]{}));
        }
        db.delete(model).execute();
        if(model instanceof Product){
            productNotify(autoId);
            return;
        }
        Integer proAutoId = (Integer)model.get("proAutoId");
        if(proAutoId!=null) productNotify(proAutoId);;
    }
    public List<Param> params(String modelName, int proAutoId, Integer autoId){
        StringBuilder sql = new StringBuilder("select P.*,MP.val "
                +"from param P left join "+modelName+"_param MP on P.autoId=MP.paramAutoId ");
        List<Object> args = new LinkedList<Object>();
        if(autoId!=null){
            sql.append(" and MP."+modelName+"AutoId=? ");
            args.add(autoId);
        }
        sql.append("where P.seq>=0 and P.proAutoId=? order by P.seq");
        args.add(proAutoId);
        return db.select(sql.toString(), args.toArray()).execute2Model(Param.class);
    }
    public void paramsSet(String modelName, int proAutoId, Integer autoId, JSONObject params)throws Exception{
        StringBuilder sql = new StringBuilder("delete from "+modelName+"_param where proAutoId=?");
        List<Object> args = new LinkedList<Object>();
        args.add(proAutoId);
        if(autoId!=null){
            sql.append(" and "+modelName+"AutoId=?");
            args.add(autoId);
        }
        db.executeSQL(sql.toString(), args.toArray()).execute();
        if(params==null || params.isEmpty()) return;
        Class<? extends Model> paramCls = Model.getModelCls(modelName+"_param");
        Model param = paramCls.newInstance();
        if(UserParam.class==paramCls) ((UserParam)param).userAutoId = autoId;
        param.set("proAutoId", proAutoId);
        BeanWrapper paramWrapper = new BeanWrapperImpl(param);
        for(Object paramAutoIdObj : params.keySet()){
            paramWrapper.setPropertyValue("paramAutoId", paramAutoIdObj);
            paramWrapper.setPropertyValue("val", params.getString(paramAutoIdObj.toString()));
            db.insert(param).execute();
        }
    }
    public <E extends Model> List<E> auths(String modelName, Class<E> authModelCls, Integer proAutoId, Integer autoId){
        List<E> auths = new LinkedList<E>();
        int[] modelAuthAutoIds = modelAuthAutoIdsGet(modelName, authModelCls, proAutoId, autoId);
        for(Integer authAutoId : modelAuthAutoIds) auths.add(get(authModelCls, authAutoId));
        return auths;
    }
    public int[] modelAuthAutoIdsGet(Class<? extends Model> modelCls, Class<? extends Model> authModelCls, Integer proAutoId, Integer autoId){
        String modelName = modelCls.getAnnotation(Table.class).value();
        return modelAuthAutoIdsGet(modelName, authModelCls, proAutoId, autoId);
    }
    public int[] modelAuthAutoIdsGet(String modelName, Class<? extends Model> authModelCls, Integer proAutoId, Integer autoId){
        String authModelName = authModelCls.getAnnotation(Table.class).value();
        StringBuilder sql = new StringBuilder("select "+authModelName+"AutoId "
                + "from "+modelName+"_"+authModelName+" "
                + "where seq>=0 ");
        List<Object> args = new LinkedList<Object>();
        if(proAutoId!=null){
            sql.append("and proAutoId=? ");
            args.add(proAutoId);
        }
        if(autoId!=null) {
            sql.append("and "+modelName+"AutoId=? ");
            args.add(autoId);
        }
        sql.append("order by seq");
        List<Integer> autoIds = db.select(sql.toString(), args.toArray()).execute2Basic(Integer.class);
        return ArrayUtils.toPrimitive(autoIds.toArray(new Integer[]{}));
    }
    public void authsSet(String modelName, Class<? extends Model> authModelCls, int proAutoId, Integer autoId, int[] authAutoIds)throws Exception{
        String authModelName = authModelCls.getAnnotation(Table.class).value();
        StringBuilder sql = new StringBuilder("delete from "+modelName+"_"+authModelName+" where proAutoId=? ");
        List<Object> args = new LinkedList<Object>();
        args.add(proAutoId);
        if(autoId!=null){
            sql.append("and "+modelName+"AutoId=?");
            args.add(autoId);
        }
        db.executeSQL(sql.toString(), args.toArray()).execute();
        if(ArrayUtils.isEmpty(authAutoIds)) return;
        Class<? extends Model> authCls = Model.getModelCls(modelName+"_"+authModelName);
        Model auth = authCls.newInstance();
        auth.set(modelName+"AutoId", autoId);
        auth.set("proAutoId", proAutoId);
        for(int i=0; i<authAutoIds.length; i++){
            auth.set(authModelName+"AutoId", authAutoIds[i]);
            auth.set("seq", i);
            db.insert(auth).execute();
        }
    }
    public AuthCalRst authCal(Integer proAutoId, int[] roleAutoIds, int[] accessAutoIds, int[] permissionAutoIds)throws Exception{
        AuthCalRst authCalRst = new AuthCalRst();
        if(proAutoId!=null){
            for(Class<? extends Model> authModelCls : AllAuthModelCls)
                authCal(authCalRst, authModelCls, modelAuthAutoIdsGet("visitor", authModelCls, proAutoId, null), "游客");
        }
        authCal(authCalRst, Role.class, roleAutoIds, "");
        authCal(authCalRst, Access.class, accessAutoIds, "");
        authCal(authCalRst, Permission.class, permissionAutoIds, "");
        return authCalRst;
    }
    public AuthCalRst authCal(int proAutoId, Integer userAutoId)throws Exception{
        AuthCalRst authCalRst = new AuthCalRst();
        for(Class<? extends Model> authModelCls : AllAuthModelCls)
            authCal(authCalRst, authModelCls, modelAuthAutoIdsGet("visitor", authModelCls, proAutoId, null), "游客");
        if(userAutoId==null) return authCalRst;
        for(Class<? extends Model> authModelCls : AllAuthModelCls)
            authCal(authCalRst, authModelCls, modelAuthAutoIdsGet(User.class, authModelCls, proAutoId, userAutoId), "");
        return authCalRst;
    }
    private void authCal(AuthCalRst authCalRst, Class<? extends Model> authModelCls, int[] authAutoIds, String from)throws Exception{
        if(ArrayUtils.isEmpty(authAutoIds))return;
        AuthDesc<Model> authDesc = new AuthDesc<Model>();
        authDesc.des = from;
        Set<Integer> authModelAutoIds = authCalRst.allAuthModelAutoIds.get(authModelCls);
        for(int authAutoId : authAutoIds){
            if(authModelAutoIds.contains(authAutoId)) continue;
            Model auth = get(authModelCls, authAutoId);
            if(auth==null) throw new Err(ErrCode.NotFound);
            authModelAutoIds.add(authAutoId);
            authDesc.auths.add(auth);
            if(Role.class!=authModelCls) continue;
            for(Class<? extends Model> subAuthModelCls : SubAuthModelCls)
                authCal(authCalRst, subAuthModelCls,
                        modelAuthAutoIdsGet("role", subAuthModelCls, null, authAutoId),
                        from+"角色"+auth.get("name"));
        }
        authCalRst.allAuths.get(authModelCls).add(authDesc);
    }
    
    public Pair<Long, List<OpLog>> oplogs(OpLog.Query query, boolean ascOrDesc, TPager pager) {
        Pair<String, List<Object>> wherePair = query.buildWhereClause();
        String oplogTbl = OpLog.class.getAnnotation(Table.class).value();
        
        StringBuilder countSql = new StringBuilder("select count(*) from ").append(oplogTbl).append(wherePair.getLeft());
        long count = db.select(countSql.toString(), wherePair.getRight().toArray()).execute2BasicOne(long.class);
        
        StringBuilder sql = new StringBuilder("select * from ").append(oplogTbl).append(wherePair.getLeft());
        List<Object> args = new LinkedList<>(wherePair.getRight());
        sql.append(" order by time ").append(ascOrDesc?"asc":"desc");
        if(pager!=null){
            sql.append("limit ?,?");
            args.add(pager.getStart());
            args.add(pager.getSize());
        }
        
        List<OpLog> oplogs = db.select(sql.toString(), args.toArray()).execute2Model(OpLog.class);
        return Pair.of(count, oplogs);
    }
    
    public void productNotify(Integer... proAutoIds){
        if(ArrayUtils.isEmpty(proAutoIds)){
            db.executeSQL("update product set lastModify=?",
                    System.currentTimeMillis()).execute();
            productAuthCache.clear();
            return;
        }
        for(int proAutoId : proAutoIds){
            db.executeSQL("update product set lastModify=? where autoId=?",
                    System.currentTimeMillis(), proAutoId).execute();
            Product product = get(Product.class, proAutoId);
            if(product==null) continue;
            productAuthCache.invalidate(product.id);
        }
    }
    
    private static final List<Class<? extends Model>> AllAuthModelCls
        = Arrays.asList(Role.class, Access.class, Permission.class);
    private static final List<Class<? extends Model>> SubAuthModelCls
        = Arrays.asList(Access.class, Permission.class);
    
    public static class AuthCalRst{
        Map<Class<? extends Model>, Set<Integer>> allAuthModelAutoIds = LazyMap.lazyMap(new HashMap<>(), cls->new HashSet<>());
        Map<Class<? extends Model>, List<AuthDesc<? extends Model>>> allAuths = LazyMap.lazyMap(new HashMap<>(), cls->new LinkedList<>());
        @SuppressWarnings("unchecked")
        public <E extends Model> List<AuthDesc<E>> getAuthDescs(Class<E> authModelCls){
            Object list = allAuths.get(authModelCls);
            return (List<AuthDesc<E>>)list;
        }
        public <E extends Model> List<E> getAuths(Class<E> authModelCls){
            List<E> auths = new LinkedList<E>();
            List<AuthDesc<E>> authDescs = getAuthDescs(authModelCls);
            for(AuthDesc<E> authDesc : authDescs) auths.addAll(authDesc.auths);
            return auths;
        }
    }
    @Data
    static class AuthDesc<E>{
        String des;
        List<E> auths = new LinkedList<E>();
    }
    
}

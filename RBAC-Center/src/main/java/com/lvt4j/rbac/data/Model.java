package com.lvt4j.rbac.data;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.lvt4j.basic.TDB.Table;
import com.lvt4j.basic.TReflect;
import com.lvt4j.rbac.data.model.Access;
import com.lvt4j.rbac.data.model.Param;
import com.lvt4j.rbac.data.model.Permission;
import com.lvt4j.rbac.data.model.Product;
import com.lvt4j.rbac.data.model.Role;
import com.lvt4j.rbac.data.model.RoleAccess;
import com.lvt4j.rbac.data.model.RolePermission;
import com.lvt4j.rbac.data.model.User;
import com.lvt4j.rbac.data.model.UserAccess;
import com.lvt4j.rbac.data.model.UserParam;
import com.lvt4j.rbac.data.model.UserPermission;
import com.lvt4j.rbac.data.model.UserRole;
import com.lvt4j.rbac.data.model.VisitorAccess;
import com.lvt4j.rbac.data.model.VisitorParam;
import com.lvt4j.rbac.data.model.VisitorPermission;
import com.lvt4j.rbac.data.model.VisitorRole;

/**
 * @author LV
 */
public abstract class Model{

    private static final Map<String, Class<? extends Model>> AllModelCls = new HashMap<String, Class<? extends Model>>();
    private static final Map<Class<? extends Model>, List<Field>> LikeFields = new HashMap<Class<? extends Model>, List<Field>>();
    private static final Map<Class<? extends Model>, List<Field>> UniqueFields = new HashMap<Class<? extends Model>, List<Field>>();
    
    static{
        Class<?>[] allModelCls = new Class<?>[]{Product.class, User.class,
                Param.class, Access.class, Permission.class,
                Role.class, RoleAccess.class, RolePermission.class,
                VisitorParam.class, VisitorRole.class, VisitorAccess.class, VisitorPermission.class,
                UserParam.class, UserRole.class, UserAccess.class, UserPermission.class};
        try{
            for(Class<?> cls : allModelCls){
                @SuppressWarnings("unchecked")
                Class<? extends Model> modelCls = (Class<? extends Model>)cls;
                AllModelCls.put(modelCls.getAnnotation(Table.class).value(), modelCls);
                List<Field> likeFields = new LinkedList<Field>();
                for(Field field : TReflect.allField(modelCls)){
                    if(!field.isAnnotationPresent(Like.class)) continue;
                    likeFields.add(field);
                }
                LikeFields.put(modelCls, likeFields);
                List<FieldSorter> fieldSorters = new LinkedList<FieldSorter>();
                for(Field field : TReflect.allField(modelCls)){
                    Unique unique = field.getAnnotation(Unique.class);
                    if(unique==null) continue;
                    FieldSorter fieldSorter = new FieldSorter();
                    fieldSorter.field = field;
                    fieldSorter.seq = unique.seq();
                    fieldSorters.add(fieldSorter);
                }
                Collections.sort(fieldSorters);
                List<Field> uniqueFields = new LinkedList<Field>();
                for(FieldSorter fieldSorter : fieldSorters) uniqueFields.add(fieldSorter.field);
                UniqueFields.put(modelCls, uniqueFields);
            }
        }catch(Exception e){
            throw new RuntimeException("初始化扫描数据库model包异常!", e);
        }
    }
    
    public static Class<? extends Model> getModelCls(String modelName){
        return AllModelCls.get(modelName);
    }
    
    public static List<Field> getLikeFields(Class<? extends Model> modelCls){
        return LikeFields.get(modelCls);
    }
    
    public static List<Field> getUniqueFields(Class<? extends Model> modelCls){
        return UniqueFields.get(modelCls);
    }
    
    public Object get(String fieldName)throws Exception{
        Field field = TReflect.field(getClass(), fieldName);
        if(field==null) return null;
        return field.get(this);
    }
    public void set(String fieldName, Object val)throws Exception{
        Field field = TReflect.field(getClass(), fieldName);
        if(field==null) return;
        field.set(this, val);
    }
    public void set(Map<String, String> modelData)throws Exception{
        BeanWrapper beanWrapper = new BeanWrapperImpl(this);
        for(Entry<String, String> entry : modelData.entrySet()){
            String fieldName = entry.getKey();
            Field field = TReflect.field(getClass(), fieldName);
            if(field==null) continue;
            beanWrapper.setPropertyValue(fieldName, entry.getValue());
        }
    }
    
    static class FieldSorter implements Comparable<FieldSorter>{
        Field field;
        int seq;

        @Override
        public int compareTo(FieldSorter o){
            return Integer.compare(seq, o.seq);
        }
    }
}

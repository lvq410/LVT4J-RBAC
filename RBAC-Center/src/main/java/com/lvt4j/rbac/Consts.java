package com.lvt4j.rbac;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.basic.TPager;
import com.lvt4j.basic.TReflect;
import com.lvt4j.basic.TScan;
import com.lvt4j.rbac.data.Like;
import com.lvt4j.rbac.data.Model;
import com.lvt4j.rbac.data.Unique;
import com.lvt4j.rbac.service.Dao;

/**
 * 各种全局常量
 * @author LV
 */
public class Consts {
    
    /** 根java包 */
    public static final String BasePackage = Consts.class.getPackage().getName();
    
    /** app根文件夹 */
    public static final File AppFolder = new File(System.getProperty("user.dir"));
    /** config文件夹 */
    public static final File ConfFolder = new File(AppFolder, "config");
    /** res文件夹 */
    public static final File ResFolder = new File(AppFolder, "res");
    
    
    /** 数据库文件名 */
    public static final String DBFileName = "rbac.db";
    public static TDB DB;
    public static Dao Dao;
    
    public static final Map<String, Class<? extends Model>> AllBaseModelCls = new HashMap<String, Class<? extends Model>>();
    public static final Map<Class<? extends Model>, List<Field>> LikeFields = new HashMap<Class<? extends Model>, List<Field>>();
    public static final Map<Class<? extends Model>, List<Field>> UniqueFields = new HashMap<Class<? extends Model>, List<Field>>();
    
    public static final Class<?>[] SupportHandlerMethodTypes = new Class<?>[]{
        JSONObject.class, TPager.class, String[].class, int[].class};
    
    /** velocity的模板文件相对路径 */
    public static final String VelocityTplContextPath = "web/vm/";
    
    /** 各种错误码 */
    public static final class ErrCode{
        public static final int NotFound = 404;
        public static final int Duplicate = 501;
    }
    
    static{
        try{
            for(Class<?> cls : TScan.scanClass(BasePackage+".data.model")){
                @SuppressWarnings("unchecked")
                Class<? extends Model> modelCls = (Class<? extends Model>)cls;
                AllBaseModelCls.put(modelCls.getAnnotation(Table.class).value(), modelCls);
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
    
    static class FieldSorter implements Comparable<FieldSorter>{
        Field field;
        int seq;

        @Override
        public int compareTo(FieldSorter o){
            return Integer.compare(seq, o.seq);
        }
    }
    
}

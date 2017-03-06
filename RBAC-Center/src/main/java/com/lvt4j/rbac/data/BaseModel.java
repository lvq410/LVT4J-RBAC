/**
 * @(#)BaseBean.java, 2017年3月6日. 
 * 
 * Copyright 2017 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.lvt4j.rbac.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lvt4j.basic.TReflect;
import com.lvt4j.basic.TDB.Col;

public abstract class BaseModel extends SeqModel{

    private static final Map<Class<?>, List<Field>> likeFields = new HashMap<Class<?>, List<Field>>();
    private static final Map<Class<?>, List<Field>> uniqueFields = new HashMap<Class<?>, List<Field>>();
    
    public static List<Field> likeFields(Class<?> modelCls){
        List<Field> fields = likeFields.get(modelCls);
        if(fields!=null) return fields;
        fields = new LinkedList<Field>();
        for(Field field : TReflect.allField(modelCls)){
            if(!field.isAnnotationPresent(Like.class)) continue;
            fields.add(field);
        }
        likeFields.put(modelCls, fields);
        return fields;
    }

    public static List<Field> uniqueFields(Class<?> modelCls){
        List<Field> fields = uniqueFields.get(modelCls);
        if(fields!=null) return fields;
        List<FieldSorter> fieldSorters = new LinkedList<FieldSorter>();
        for(Field field : TReflect.allField(modelCls)){
            Unique unique = field.getAnnotation(Unique.class);
            if(unique == null) continue;
            FieldSorter fieldSorter = new FieldSorter();
            fieldSorter.field = field;
            fieldSorter.seq = unique.seq();
            fieldSorters.add(fieldSorter);
        }
        Collections.sort(fieldSorters);
        fields = new LinkedList<Field>();
        for(FieldSorter fieldSorter : fieldSorters) fields.add(fieldSorter.field);
        uniqueFields.put(modelCls, fields);
        return fields;
    }

    @Col(id=true, autoId=true)
    public Integer aId;
    
    public String des;
    
    static class FieldSorter implements Comparable<FieldSorter>{
        Field field;
        int seq;

        @Override
        public int compareTo(FieldSorter o){
            return Integer.compare(seq, o.seq);
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Like {}
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Unique{int seq() default 0;}
    
}

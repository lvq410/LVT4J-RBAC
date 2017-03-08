package com.lvt4j.rbac.data;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.lvt4j.basic.TReflect;

/**
 * @author LV
 */
public abstract class Model{

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
    
}

package com.lvt4j.rbac.dto;

import static com.lvt4j.rbac.Utils.stack;
import static java.lang.String.format;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * 统一的json返回数据格式<br>
 * <pre>{
 *   err:0,    //错误码
 *   msg:'',   //消息,如错误原因或者堆栈等,可没有
 *   stack:[], //错误的堆栈数据,可没有
 *   data:{}   //需要传的数据,各种json格式都可以,可没有
 * }</pre>
 * @author LV
 */
@Data
public class JsonResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 错误码 */
    public int err;
    /** 消息,如错误原因等,可没有 */
    public String msg;
    /** 错误的堆栈数据,可没有 */
    public List<String> stack;
    /** 需要传的数据,各种格式都可以,可没有 */
    public Object data;
    
    public static JsonResult success() {
        return success(null, null);
    }
    
    public static JsonResult success(Object data) {
        return success(data, null);
    }
    
    public static JsonResult success(Object data, String msg) {
        JsonResult rst = new JsonResult();
        rst.err = 0;
        if(StringUtils.isNotEmpty(msg)) rst.msg = msg;
        if(data!=null) rst.data = data;
        return rst;
    }
    
    /** 默认500错误码 */
    public static JsonResult fail() {
        return fail(500, null, null);
    }
    
    public static JsonResult fail(int errCode) {
        return fail(errCode, null, null);
    }
    
    /** 默认500错误码 */
    public static JsonResult fail(String msg) {
        return fail(500, msg, null);
    }
    /** 默认500错误码 */
    public static JsonResult fail(String msg, Throwable e) {
        return fail(500, msg, e);
    }
    
    public static JsonResult fail(int errCode, String msg) {
        return fail(errCode, msg, null);
    }
    
    public static JsonResult fail(int errCode, String msg, Throwable e) {
        JsonResult rst = new JsonResult();
        rst.err = errCode;
        if(StringUtils.isNotEmpty(msg)) rst.msg = msg;
        if(e!=null) rst.stack = stack(e);
        return rst;
    }

    /** 替换data字段 */
    public JsonResult data(Object data) {
        this.data = data;
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public <D> D data() {
        return (D) data;
    }
    
    /**
     * data字段自动初始化为map(如果data字段已有且不是map，抛异常)，并向其中填入kv数据
     * @param key
     * @param val
     * @return
     */
    public JsonResult dataPut(Object key, Object val) {
        Map<Object, Object> data = mapDataGet();
        data.put(key, val);
        return this;
    }
    
    /** @see #dataPut(Object, Object) */
    public JsonResult dataPutAll(Map<?, ?> datas) {
        mapDataGet().putAll(datas);
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public <V> V dataGet(Object key) {
        return (V) mapDataGet().get(key);
    }
    
    @SuppressWarnings("unchecked")
    private Map<Object, Object> mapDataGet() {
        if(data instanceof Map) return (Map<Object, Object>) data;
        if (data==null) return (Map<Object, Object>) (data = new HashMap<Object, Object>());
        throw new IllegalArgumentException(format("JsonResult的data数据[%s]已存在且不是个map,不能往data里继续put", data));
    }
}

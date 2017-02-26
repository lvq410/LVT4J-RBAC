package com.lvt4j.rbac.data.bean.base;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BaseParam{
    
    public String key;
    public String val;
    
    public static Map<String, String> toMap(List<BaseParam> baseParams) {
        Map<String, String> param = new TreeMap<String, String>();
        for(BaseParam baseParam : baseParams)
            param.put(baseParam.key, baseParam.val);
        return param;
    }
    
}

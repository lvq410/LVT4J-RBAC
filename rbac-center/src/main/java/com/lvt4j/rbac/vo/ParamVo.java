package com.lvt4j.rbac.vo;

import com.lvt4j.rbac.po.Param;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author LV on 2020年8月6日
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class ParamVo extends Param {

    public String val;
    
}

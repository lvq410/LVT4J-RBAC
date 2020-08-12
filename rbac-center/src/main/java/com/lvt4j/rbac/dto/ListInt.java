package com.lvt4j.rbac.dto;

import java.util.ArrayList;
import java.util.Collection;

/**
 * int列表，存储在数据库为jsonarr
 * @author lichenxi on 2018年3月22日
 */
public class ListInt extends ArrayList<Integer> {
    private static final long serialVersionUID = 7232042635255952301L;

    public ListInt() {
        super();
    }

    public ListInt(Collection<? extends Integer> c) {
        super(c);
    }

    public ListInt(int initialCapacity) {
        super(initialCapacity);
    }
    
}
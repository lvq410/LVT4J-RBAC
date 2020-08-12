package com.lvt4j.rbac.dto;

import java.util.ArrayList;
import java.util.Collection;

/**
 * String列表
 * @author lichenxi on 2019年5月7日
 */
public class ListStr extends ArrayList<String> {
    private static final long serialVersionUID = 7232042635255952301L;

    public ListStr() {
        super();
    }

    public ListStr(Collection<String> c) {
        super(c);
    }

    public ListStr(int initialCapacity) {
        super(initialCapacity);
    }
    
}

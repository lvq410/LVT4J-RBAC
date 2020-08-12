package com.lvt4j.rbac.dto;

import java.util.HashMap;
import java.util.Map;

public class MapIntStr extends HashMap<Integer, String>{
    private static final long serialVersionUID = 3458959407942725221L;

    public MapIntStr() {
        super();
    }

    public MapIntStr(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public MapIntStr(int initialCapacity) {
        super(initialCapacity);
    }
    
    public MapIntStr(Map<? extends Integer, ? extends String> m) {
        super(m);
    }
    
}
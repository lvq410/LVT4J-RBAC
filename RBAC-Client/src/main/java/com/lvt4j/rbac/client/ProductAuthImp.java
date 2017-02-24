package com.lvt4j.rbac.client;

import com.lvt4j.rbac.data.AbstractProductAuth;
import com.lvt4j.rbac.data.UserAuth;

class ProductAuthImp extends AbstractProductAuth{

    public ProductAuthImp(String proId){
        super(proId, 1000);
    }

    @Override
    protected UserAuth loadUserAuth(String userId){
        // TODO Auto-generated method stub
        return null;
    }

}

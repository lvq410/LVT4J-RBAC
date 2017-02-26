package com.lvt4j.rbac;



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

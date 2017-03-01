package com.lvt4j.rbac;

class ProductAuthImp extends AbstractProductAuth{

    private String rbacCenterAddress;
    
    
    public ProductAuthImp(String proId, int capacity, String rbacCenterAddress){
        super(proId, capacity);
        this.rbacCenterAddress = rbacCenterAddress;
    }

    @Override
    protected UserAuth loadUserAuth(String userId){
        // TODO Auto-generated method stub
        return null;
    }

}

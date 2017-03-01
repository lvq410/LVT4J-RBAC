package com.lvt4j.rbac;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

class ProductAuth4Client extends AbstractProductAuth{

    private static final String UrlTpl_ProLastModify = "http://%s/inner/proLastModify";
    
    private long lastModify = 0;
    
    private String rbacCenterAddr;
    private Timer rbacCenterSyncTimer;
    private TimerTask rbacCenterSync = new TimerTask(){
        @Override
        public void run(){
            
        }
    };
    
    public ProductAuth4Client(String proId, int capacity,
            String rbacCenterAddr, int rbacCenterSyncInterval){
        super(proId, capacity);
        this.rbacCenterAddr = rbacCenterAddr;
        rbacCenterSyncTimer = new Timer();
        rbacCenterSyncTimer.schedule(rbacCenterSync, 0, rbacCenterSyncInterval*60*1000);
    }

    @Override
    protected UserAuth loadUserAuth(String userId){
        return null;
    }
    
    @Override
    public void destory(){
        rbacCenterSyncTimer.cancel();
    }

    private byte[] loadUrl(String spec) throws Exception {
        HttpURLConnection conn = null;
        
        
        URL url = new URL(spec);
        conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(200);
        conn.setReadTimeout(200);
        conn.connect();
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len=in.read(buf))!=-1) baos.write(buf, 0, len);
        in.close();
        conn.disconnect();
        return baos.toByteArray();
    }
    
}

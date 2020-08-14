package com.lvt4j.rbac.cluster;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.condition.IsSlave;

import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月4日
 */
@Slf4j
@RestController
@RequestMapping("cluster")
@ManagedResource(objectName="Slave:type=Slave")
@Conditional(IsSlave.class)
public class Slave extends Thread implements EventBusPublisher,ClusterStator {

    private static final TypeReference<List<MemberStatus>> MemberStatsRef = new TypeReference<List<MemberStatus>>() {};
    
    @Value("${server.master_host}")
    private String masterHost;
    @Value("${server.master_port}")
    private int masterPort;
    
    @Value("${server.publish_host}")
    private String host;
    @Value("${server.publish_port}")
    private int port;
    
    @Getter(onMethod=@__({@ManagedAttribute}))
    private String masterOrigin;
    
    private URL masterPubUrl;
    
    private URL masterSubUrl;
    
    private URL masterStatsUrl;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private BroadcastMsgHandler handler;
    
    private volatile boolean destory;
    
    @PostConstruct
    private void init() throws Exception {
        log.info("本节点为从节点：{}:{}，主节点为：{}:{}", host, port, masterHost, masterPort);
        
        masterOrigin = "http://"+masterHost+":"+masterPort;
        masterSubUrl = UriComponentsBuilder.fromHttpUrl(masterOrigin+"/cluster/subscribe")
            .queryParam("host", host).queryParam("port", port).build().toUri().toURL();
        masterPubUrl = new URL(masterOrigin+"/cluster/publish/4slave");
        masterStatsUrl = new URL(masterOrigin+"/cluster/stats/4slave");
        setName("Slave");
        start();
    }
    
    @PreDestroy
    private void destory() {
        destory = true;
        interrupt();
        try{
            join(1000);
        }catch(Exception e){
            log.warn("Slave close exception", e);
        }
    }
    
    @Override
    public final void run() {
        while(!destory){
            try{
                subMaster();
            }catch(Throwable e){
                if(destory) return;
                log.error("与主节点的连接断开", e);
            }
            if(destory) return;
            try{
                Thread.sleep(10000);
            }catch(Exception ig){}
        }
    }
    private void subMaster() throws Throwable {
        @Cleanup("disconnect") HttpURLConnection cnn = (HttpURLConnection) masterSubUrl.openConnection();
        cnn.setConnectTimeout(1000);
        @Cleanup InputStream is = cnn.getInputStream();
        log.info("连接主节点[{}:{}]成功", masterHost, masterPort);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while(!destory){
            line = reader.readLine();
            if(line==null) continue;
            if(log.isTraceEnabled()) log.trace("广播消息:{}", line);
            if(!line.startsWith("data:")) continue;
            line = line.substring(5);
            if(StringUtils.isBlank(line)) continue;
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(line)));
            BroadcastMsg4Center msg;
            try{
                msg = (BroadcastMsg4Center) ois.readObject();
            }catch(Throwable e){
                log.error("非法的广播消息:{}", line, e);
                continue;
            }
            handler.onBroadcastMsg(msg);
        }
    }
    
    @Override
    public void publish(BroadcastMsg4Center msg) { //通知master发广播
        if(log.isTraceEnabled()) log.trace("请求主节点发广播:{}", msg);
        try{
            @Cleanup("disconnect") HttpURLConnection cnn = (HttpURLConnection) masterPubUrl.openConnection();
            cnn.setDoOutput(true);
            cnn.setConnectTimeout(1000);
            cnn.setReadTimeout(2000);
            cnn.setRequestProperty(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
            @Cleanup OutputStream os = cnn.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(msg);
            oos.close();
            @Cleanup InputStream is = cnn.getInputStream();
        }catch(Throwable e){
            log.warn("请求主节点发广播:{}失败", msg, e);
        }
    }
    
    @Override
    @SneakyThrows
    public List<MemberStatus> getMemberStats() { //从节点利用master获取集群状态
        @Cleanup("disconnect") HttpURLConnection cnn = (HttpURLConnection) masterStatsUrl.openConnection();
        cnn.setConnectTimeout(1000);
        cnn.setReadTimeout(5000);
        @Cleanup InputStream is = cnn.getInputStream();
        return objectMapper.readValue(is, MemberStatsRef);
    }
    
}
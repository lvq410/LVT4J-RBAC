package com.lvt4j.rbac.cluster;

import static com.lvt4j.rbac.Utils.sse;
import static com.lvt4j.rbac.Utils.sses;
import static com.lvt4j.rbac.Utils.ssesRaw;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.BroadcastMsg4Center.Handshake;
import com.lvt4j.rbac.condition.IsMaster;
import com.lvt4j.rbac.service.ClientService;
import com.lvt4j.rbac.service.ClientService.ClientInfo;
import com.lvt4j.rbac.service.SingleThreader;

import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月3日
 */
@Slf4j
@Component
@Conditional(IsMaster.class)
@ManagedResource(objectName="Master:type=Master")
public class Master implements EventBusPublisher, ClusterStator {
    
    private static final TypeReference<Collection<ClientInfo>> ClientsRef = new TypeReference<Collection<ClientInfo>>() {};
    
    @Value("${server.publish_host}")
    private String host;
    @Value("${server.publish_port}")
    private int port;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SingleThreader singleThreader;
    
    @Autowired
    private BroadcastMsgHandler handler;
    
    @Autowired
    private ClientService clientService;
    
    /** master任期 */
    private final long masterTerm = System.currentTimeMillis();
    /** 广播消息的递增id */
    private final AtomicLong eventbusMsgIdx = new AtomicLong();
    
    /** 所有从节点长链 */
    private final Map<SseEmitter, SlaveInfo> slaves = new ConcurrentHashMap<>();
    
    @PostConstruct
    private void init() {
        log.info("本节点为主节点：{}:{}", host, port);
        
        handler.onBroadcastMsg(new Handshake(masterTerm, eventbusMsgIdx.get()));
    }
    
    public SseEmitter subscribe(String host, int port) throws Exception {
        SlaveInfo slaveInfo = new SlaveInfo();
        slaveInfo.host = host;
        slaveInfo.port = port;
        
        SseEmitter emitter = new SseEmitter(0L);
        
        emitter.onTimeout(()->slaves.remove(emitter));
        emitter.onError(e->slaves.remove(emitter));
        emitter.onCompletion(()->slaves.remove(emitter));
        
        singleThreader.enqueue(()->{
            sse(emitter, new Handshake(masterTerm, eventbusMsgIdx.get()), this::onSendException);
            slaves.put(emitter, slaveInfo);
        });
        
        return emitter;
    }
    
    @Override
    @SneakyThrows
    public void publish(BroadcastMsg4Center msg) {
        if(log.isTraceEnabled()) log.trace("入队广播消息{}", msg);
        singleThreader.enqueue(()->{
            BroadcastMsg4Center m = msg.msgIdx(masterTerm, eventbusMsgIdx.incrementAndGet());
            if(log.isTraceEnabled()) log.trace("广播消息{}", m);
            handler.onBroadcastMsg(m);
            
            sses(slaves.keySet(), m, this::onSendException);
        });
    }
    
    @SneakyThrows
    @Scheduled(cron="0/10 * * * * ?")
    public void heartbeat() {
        singleThreader.enqueue(()->{
            ssesRaw(slaves.keySet(), EMPTY, this::onSendException);
        });
    }
    
    private void onSendException(SseEmitter emitter) {
        SlaveInfo slaveInfo = slaves.get(emitter);
        if(slaveInfo==null) return;
        log.info("断开从节点[{}:{}]的连接", slaveInfo.host, slaveInfo.port);
        slaves.remove(emitter);
    }

    @Override
    @ManagedOperation
    public List<MemberStatus> getMemberStats() {
        List<MemberStatus> stats = new LinkedList<>();
        MemberStatus master = new MemberStatus();
        master.address = host+":"+port;
        master.status = "master";
        master.clients = clientService.getClients();
        stats.add(master);
        slaves.values().parallelStream().map(slave->{
            MemberStatus m = new MemberStatus();
            m.address = slave.host+":"+slave.port;
            m.status = "slave";
            String clientsRaw = slave.http("/cluster/clients");
            if(clientsRaw==null){
                m.status = "unreachable";
            }else{
                try{
                    m.clients = objectMapper.readValue(clientsRaw, ClientsRef);
                }catch(Exception e){
                    log.warn("解析从节点客户端结果失败:{}", clientsRaw, e);
                }
            }
            return m;
        }).collect(toList()).forEach(stats::add);
        Collections.sort(stats, (m1,m2)->m1.address.compareTo(m2.address));
        return stats;
    }
    
    @Data
    static class SlaveInfo {
        public String host;
        public int port;
        
        public String http(String path) {
            try{
                @Cleanup("disconnect") HttpURLConnection cnn = (HttpURLConnection) new URL("http://"+host+":"+port+path).openConnection();
                cnn.setConnectTimeout(1000);
                cnn.setReadTimeout(2000);
                @Cleanup InputStream is = cnn.getInputStream();
                return IOUtils.toString(is, Charset.defaultCharset());
            }catch(Throwable e){
                log.warn("由从节点[{}:{}]获取数据{}失败", host, port, path, e);
                return null;
            }
        }
    }
    
}
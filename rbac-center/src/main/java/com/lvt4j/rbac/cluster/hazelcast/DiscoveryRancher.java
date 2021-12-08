package com.lvt4j.rbac.cluster.hazelcast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author lichenxi on 2020年6月6日
 */
@Slf4j
@Configuration
@ConfigurationProperties("hazelcast.discover.rancher")
@ConditionalOnProperty(name="hazelcast.discover.mode",havingValue="rancher")
class DiscoveryRancher implements Discovery {

    @Value("${hazelcast.port}")
    private int port;
    
    /** rancher api 地址 */
    @Setter
    private String prefix;
    /** rancher api 用accessKey */
    @Setter
    private String accessKey;
    /** rancher api 用secretKey */
    @Setter
    private String secretKey;
    /** 请求 rancher api 超时时间(ms) */
    @Setter
    private int timeout = 30000;
    /** pod部署的projectId */
    @Setter
    private String projectId;
    /** kubernetes在每个容器上建立的namespace文件路径 */
    @Setter
    private String k8sNamespaceFile = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
    /** pod部署的namespaceId */
    @Setter
    private String namespaceId;
    /** pod部署的workloadId */
    @Setter
    private String workloadId;
    
    //======================================================================变量
    private String hostname;
    
    private String auth;
    
    @Setter(AccessLevel.PRIVATE)
    private int quorum = 1;
    private boolean quorumValid;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @PostConstruct
    private void init() throws Throwable {
        hostname = Inet4Address.getLocalHost().getHostName();
        detectNamespaceId();
        detectProjectId();
        detectWorkloadId();
        loadQuorum();
    }
    
    public List<String> getSeeds() throws Throwable {
        JsonNode rst = callRancherApi("/project/%s/pods?workloadId=%s", projectId, workloadId);
        JsonNode data = rst.path("data");
        List<String> seeds = new ArrayList<>(data.size());
        for(int i=0; i<data.size(); i++){
            String ip = data.get(i).at("/status/podIp").asText();
            if(StringUtils.isBlank(ip)) continue;
            seeds.add(ip+":"+port);
        }
        Collections.sort(seeds);
        return seeds;
    }
    
    @Override
    public int getQuorum() {
        checkAndRefreshQuorum();
        return quorum;
    }
    @Override
    public void invalidateQuorumCache() {
        quorumValid = false;
        checkAndRefreshQuorum();
    }
    private void checkAndRefreshQuorum() {
        if(quorumValid) return;
        synchronized(this){
            if(quorumValid) return;
            try{
                loadQuorum();
            }catch(Throwable e){
                log.warn("刷新Hazelcast quorum异常", e);
            }
        }
    }
    private void loadQuorum() throws Throwable {
        quorum = workloadReplica()/2+1;
        quorumValid = true;
    }
    
    private void detectNamespaceId() throws Exception {
        if(StringUtils.isNotBlank(namespaceId)) return;
        File namespaceFile = new File(k8sNamespaceFile);
        if(!namespaceFile.exists()) throw new FileNotFoundException(String.format("Kubernetes namespace file[%s] not exists, are you in a k8s environment?", k8sNamespaceFile));
        namespaceId = FileUtils.readFileToString(namespaceFile, Charset.defaultCharset());
        if(StringUtils.isBlank(namespaceId)) throw new IllegalStateException(String.format("Can't get namspaceId from kubernetes namespace file: %s", k8sNamespaceFile));
    }
    private void detectProjectId() throws Exception {
        if(StringUtils.isNotBlank(projectId)) return;
        try{
            String ip = Inet4Address.getLocalHost().getHostAddress();
            JsonNode rst = callRancherApi("/projects");
            JsonNode data = rst.path("data");
            for(int i=0; i<data.size(); i++){
                String proId = data.path(i).path("id").asText();
                if(StringUtils.isBlank(proId)) continue;
                JsonNode podRst = callRancherApi("/project/%s/pods?name=%s", proId, hostname);
                JsonNode podRstDatas = podRst.path("data");
                if(podRstDatas.size()==0) continue;
                JsonNode podRstData = IntStream.range(0, podRstDatas.size()).mapToObj(podRstDatas::path).filter(d->ip.equals(d.at("/status/podIp").asText())).findFirst().orElse(null);
                if(podRstData==null) continue;
                projectId = proId;
                if(StringUtils.isBlank(workloadId)){
                    String workloadId = podRstData.path("workloadId").asText();
                    if(StringUtils.isNotBlank(workloadId)) this.workloadId = workloadId;
                }
                return;
            }
            throw new Exception(String.format("There is no project with name [%s] and has pod [%s]", namespaceId, hostname));
        }catch(Exception e){
            throw new IllegalStateException(String.format("Can't find project has pod [%s] from rancher api [/projects?name=%s]", hostname, namespaceId), e);
        }
    }
    private void detectWorkloadId() throws Exception {
        if(StringUtils.isNotBlank(workloadId)) return;
        try{
            JsonNode podRst = callRancherApi("/project/%s/pods?name=%s", projectId, hostname);
            JsonNode podRstDatas = podRst.path("data");
            if(podRstDatas.size()==0) throw new IllegalStateException("Pod not exist");
            String ip = Inet4Address.getLocalHost().getHostAddress();
            JsonNode podRstData = IntStream.range(0, podRstDatas.size()).mapToObj(podRstDatas::path).filter(d->ip.equals(d.at("/status/podIp").asText())).findFirst().orElse(null);
            if(podRstData==null) throw new IllegalStateException(String.format("Pod's ip is not %s", ip));
            String workloadId = podRstData.path("workloadId").asText();
            if(StringUtils.isBlank(workloadId)) throw new IllegalStateException("WorkloadId property [/data/[]/workloadId] not present");
            this.workloadId = workloadId;
        }catch(Throwable e){
            throw new IllegalStateException(String.format("Can't find pod [%s] in project [%s] from rancher api [/project/%s/pods?name=%s]", hostname, projectId, projectId, hostname), e);
        }
    }
    
    private int workloadReplica() throws Throwable {
        JsonNode rst = callRancherApi("/project/%s/workloads/%s", timeout, projectId, workloadId);
        int minMembers = rst.at("/deploymentStatus/updatedReplicas").asInt();
        if(minMembers==0) throw new IllegalStateException(String.format("Get workload replica numbers from rancher api [/project/%s/workloads/%s] failed.Api response:\n%s", projectId, workloadId, rst));
        return minMembers;
    }
    
    private JsonNode callRancherApi(String uri, String... args) throws IOException {
        return callRancherApi(uri, 10000, args);
    }
    
    @SneakyThrows(UnsupportedEncodingException.class)
    private JsonNode callRancherApi(String uri, int timeout, String... args) throws IOException {
        if(args!=null){
            for(int i=0; i<args.length; i++){
                args[i] = URLEncoder.encode(args[i], "utf8");
            }
        }
        String url = prefix + String.format(uri, (Object[])args);
        try{
            @Cleanup("disconnect") HttpURLConnection hc = (HttpURLConnection) new URL(url).openConnection();
            hc.addRequestProperty("Authorization", auth());
            hc.addRequestProperty("Accept", "application/json");
            hc.setConnectTimeout(500);
            hc.setReadTimeout(timeout);
            @Cleanup InputStream is = hc.getInputStream();
            String rst = IOUtils.toString(is, Charset.defaultCharset());
            if(log.isTraceEnabled()) log.trace("Call rancher api {} rst {}", url, rst);
            return objectMapper.readTree(rst);
        }catch(Exception e){
            throw new IOException(String.format("Call rancher api failed: %s", url), e);
        }
    }
    private String auth() {
        if(auth!=null) return auth;
        synchronized(this){
            return auth = "Basic "+Base64.getEncoder().encodeToString((accessKey+":"+secretKey).getBytes());
        }
    }
    
    @Endpoint(id="hazelcast-quorum")
    @ManagedResource(objectName="Discovery:des=查询、重载与手动设置quorum")
    @Component("hazelcast-quorum-actuator")
    @ConditionalOnProperty(name="hazelcast.discover.mode",havingValue="rancher")
    public static class QuorumAcutator {
        
        @Autowired
        private DiscoveryRancher discovery;
        
        @ReadOperation
        @ManagedAttribute
        public int getQuorum() {
            return discovery.getQuorum();
        }
        
        @ManagedOperation(description="重新从rancher接口拉取quorum值并更新到本地内存缓存")
        public int reloadQuorum() throws Throwable {
            discovery.loadQuorum();
            return discovery.getQuorum();
        }
        
        @WriteOperation
        @ManagedAttribute(description="本地内存中缓存的quorum值\n"
            +"集群有节点变动时缓存会失效，会重新从rancher接口拉取quorum值\n"
            +"因此除非临时需要，否则Rancher服务发现模式不推荐使用该方法更新本地缓存的quorum值")
        public int setQuorum(int quorum) throws Exception {
            log.info("Hazelcast quorum set manually to {}", quorum);
            discovery.setQuorum(quorum);
            return quorum;
        }

    }
}
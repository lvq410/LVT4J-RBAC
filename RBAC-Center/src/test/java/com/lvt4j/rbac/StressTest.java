package com.lvt4j.rbac;

import static java.util.Arrays.asList;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * 压力测试
 * @author LV
 */
@SpringBootApplication
public class StressTest extends RbacCenter implements UncaughtExceptionHandler {
    
    public static void main(String[] args) throws Exception {
        new File("rbac.db").delete();
        RbacCenter.main(args);
        Tester.start();
        Tester.join();
        System.exit(Ex==null?0:-1);
    }
    
    private static Throwable Ex;
    
    private static Tester Tester;
    
    @Autowired
    Tester tester;
    
    @PostConstruct
    private void init() {
        Tester = tester;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Ex = e;
        e.printStackTrace();
    }
    
}
@Slf4j
@Service
class Tester extends Thread {
    
    @Autowired
    TestDataIniter testDatar;
    
    @Value("${server.port}")
    private int port;
    
    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    @Override
    @SneakyThrows
    public void run() {
        testDatar.init();
        log.info("并发数:{}", Runtime.getRuntime().availableProcessors());
        log.info("无写入纯查询压测...");
        queryTest(10000);
        log.info("边更新项目边查询压测...");
        scheduleChangePro0(10000, 1000);
        queryTest(10000);
    }
    @SneakyThrows
    private void queryTest(int n) {
        CountDownLatch latch = new CountDownLatch(n);
        Statis suc = new Statis();
        Statis fail = new Statis();
        for(int i=0; i<n; i++){
            int no = i;
            pool.execute(()->{
                long begin = System.currentTimeMillis();
                Pair<Integer, String> post = post("/api/user/auth", "proId","pro0","userId","user"+no);
                long end = System.currentTimeMillis();
                long consume = end-begin;
                if(post.getLeft()==200){
                    suc.append(consume);
                }else {
                    fail.append(consume);
                }
                latch.countDown();
            });
        }
        latch.await();
        List<List<Object>> table = new LinkedList<>();
        table.add(asList("结果","总数","总耗时","平均耗时",".99耗时","max","min"));
        table.add(suc.row("成功"));
        table.add(fail.row("失败"));
        Statis total = new Statis();
        total.add(suc);
        total.add(fail);
        table.add(total.row("总计"));
        log.info("结果\n"+table.stream().map(r->StringUtils.join(r,"\t")).collect(Collectors.joining("\n")));
    }
    private void scheduleChangePro0(long max, long delta) {
        new Thread(()->{
            long n = 0;
            while(n<max){
                log.info("更新项目0:{}", post("/proNotify", "proAutoId",TestDataIniter.Pro0AutoId));
                n+=delta;
                try {
                    Thread.sleep(delta);
                } catch (Exception e) {}
            }
        }).start();
    }
    
    
    private Pair<Integer, String> post(String uri, Object... params) {
        Request req = Request.Post("http://localhost:"+port+uri);
        if(ArrayUtils.isNotEmpty(params)){
            Form form = Form.form();
            for(int i=0; i<params.length; i++){
                String key = String.valueOf(params[i++]);
                String val = params[i]==null?"":String.valueOf(params[i]);
                form.add(key, val);
            }
            req.bodyForm(form.build(), Charset.defaultCharset());
        }
        try{
            HttpResponse res = req.execute().returnResponse();
            int status = res.getStatusLine().getStatusCode();
            String rst = EntityUtils.toString(res.getEntity());
            return Pair.of(status, rst);
        }catch(Exception e){
            return Pair.of(500, "IOException");
        }
    }
    
}
class Statis {
    List<Long> consumes = Collections.synchronizedList(new LinkedList<>());
    AtomicLong consumeSum = new AtomicLong();
    public void append(long consume) {
        consumes.add(consume);
        consumeSum.addAndGet(consume);
    }
    public void add(Statis other) {
        consumes.addAll(other.consumes);
        consumeSum.addAndGet(other.consumeSum.get());
    }
    public List<Object> row(String type) {
        int count = consumes.size();
        long avg = consumes.isEmpty()?0:(consumeSum.get()/consumes.size());
        Collections.sort(consumes);
        long c99 = consumes.isEmpty()?0:consumes.get(consumes.size()-(consumes.size()/100));
        Long max = consumes.stream().max(Long::compareTo).orElse(null);
        Long min = consumes.stream().min(Long::compareTo).orElse(null);
        return asList(type, count, consumeSum.get(), avg, c99, max, min);
    }
}

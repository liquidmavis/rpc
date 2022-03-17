package com.cuit.utils.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 15:00
 */

@Slf4j
public final class ThreadPoolFactoryUtils {
    /**
     * 通过线程前缀来区分不同的线程池
     * key : 线程名字前缀
     * value : 线程池
     */

    private final static Map<String,ExecutorService> THREAD_POOLS = new ConcurrentHashMap<String,ExecutorService>();

    private ThreadPoolFactoryUtils() {}

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefiex){
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(threadNamePrefiex, customThreadPoolConfig,false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadName,CustomThreadPoolConfig customThreadPoolConfig){
        return createCustomThreadPoolIfAbsent(threadName, customThreadPoolConfig);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadName,CustomThreadPoolConfig customThreadPoolConfig,boolean daemon){
        ExecutorService executorService = THREAD_POOLS.computeIfAbsent(threadName, k -> createThreadPool(threadName, customThreadPoolConfig, daemon));
        if(executorService.isShutdown() || executorService.isTerminated()){
            THREAD_POOLS.remove(threadName);
            executorService = createThreadPool(threadName, customThreadPoolConfig, daemon);
            THREAD_POOLS.put(threadName, executorService);
        }
        return executorService;
    }



    private static ExecutorService createThreadPool(String threadNamePrefiex,CustomThreadPoolConfig customThreadPoolConfig,boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefiex, daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(),customThreadPoolConfig.getMaximumPoolSize(),customThreadPoolConfig.getKeepAliveTime(),
                customThreadPoolConfig.getUnit(),customThreadPoolConfig.getWorkQueue(),threadFactory);
    }

    /**
     * 创建 ThreadFactory 。如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon           指定是否为 Daemon Thread(守护线程)
     * @return ThreadFactory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if(threadNamePrefix != null) {
            if(daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon).build();
            }
            return new ThreadFactoryBuilder().setNameFormat("-%d").build();
        }
        return Executors.defaultThreadFactory();
    }

    public static void printThreadPoolStatus(ThreadPoolExecutor threadPoolExecutor){
        ScheduledExecutorService scheduledExecutorService
                = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thead-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(()->{
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPoolExecutor.getPoolSize());
            log.info("Active Threads: [{}]", threadPoolExecutor.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPoolExecutor.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPoolExecutor.getQueue().size());
            log.info("===========================================");
        },0,1,TimeUnit.SECONDS);
    }

    public static void shutDownAllThreadPool(){
        log.info("关闭所有线程");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry->{
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("关闭了线程池[{}] [{}]",entry.getKey(),executorService.isTerminated());

            try {
                executorService.awaitTermination(10,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("线程池关闭失败");
                executorService.shutdownNow();
            }
        });
    }


}

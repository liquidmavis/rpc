package com.cuit.registry.util;

import com.cuit.enums.RpcConfigEnum;
import com.cuit.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/24 20:46
 */
@Slf4j
public final class CuratorUtils {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/liquid-rpc";
    private static final Map<String,List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1";

    private CuratorUtils() {}

    public static void createPersistentNode(CuratorFramework zkClient,String path){
        //eg: /liquid-rpc/com.cuit.HelloService/127.0.0.1:9999
        try {
            if(REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("该节点以及存在,节点地址为{}",path);
            }else{
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("成功创建节点，地址{}",path);
                REGISTERED_PATH_SET.add(path);
            }
        } catch (Exception e) {
            log.error("创建节点[{}]失败",path);
        }
    }

    /**
     *
     * @param zkClient
     * @param rpcServiceName 类名 group 版本号 eg:  com.cuit.HelloSerivce2version1
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient,String rpcServiceName){
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName;
        try{
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName,zkClient);
        }catch(Exception e){
            log.error("获取孩子节点失败[{}]",servicePath);
        }
        return result;
    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTERED_PATH_SET.stream().parallel().forEach(p->{
            try{
                if(p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            }catch(Exception e){
                log.error("清除注册地址失败");
            }
        });
        log.info("所有注册在服务器的服务都被清除[{}]",REGISTERED_PATH_SET.toString());
    }

    public static CuratorFramework getZkClient(){
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null &&
                properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue())!= null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()):DEFAULT_ZOOKEEPER_ADDRESS;
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }
        RetryPolicy retryPolicy= new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try{
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)){
                throw new RuntimeException("超时");
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return zkClient;

    }

    private static void registerWatcher(String rpcServiceName,CuratorFramework zkClient)throws Exception{
        String servicePath = ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        //监听节点，当节点被更改时候，把节点对应的地址更新到map中
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework,pathChildrenCacheEvent)->{
            List<String> serviceAddress = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,serviceAddress);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();

    }
}

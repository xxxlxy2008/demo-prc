package com.demo.rpc.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ZookeeperRegistry<T> implements Registry<T> {

    private Map<String, List<ServiceInstanceListener<T>>> listeners = Maps.newConcurrentMap();

    private InstanceSerializer serializer = new JsonInstanceSerializer<>(ServerInfo.class);

    private ServiceDiscovery<T> serviceDiscovery;

    private ServiceCache<T> serviceCache;

    private String address = "localhost:2181";

    public void start() throws Exception {
        String root = "/demo/rpc";
        // 初始化CuratorFramework
        CuratorFramework client = CuratorFrameworkFactory.newClient(address, new ExponentialBackoffRetry(1000, 3));
        client.start();  // 启动Curator客户端
        // client.createContainers(root);

        // 初始化ServiceDiscovery
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServerInfo.class)
                .client(client).basePath(root)
                .serializer(serializer)
                .build();
        serviceDiscovery.start(); // 启动ServiceDiscovery

        // 创建ServiceCache，监Zookeeper相应节点的变化，也方便后续的读取
        serviceCache = serviceDiscovery.serviceCacheBuilder()
                .name("/demoService")
                .build();
//        client.start(); // 启动Curator客户端
        client.blockUntilConnected();  // 阻塞当前线程，等待连接成功
        serviceDiscovery.start(); // 启动ServiceDiscovery
        serviceCache.start(); // 启动ServiceCache
    }

    @Override
    public void registerService(ServiceInstance<T> service) throws Exception {
        serviceDiscovery.registerService(service);
    }

    @Override
    public void unregisterService(ServiceInstance service) throws Exception {
        serviceDiscovery.unregisterService(service);
    }

    @Override
    public List<ServiceInstance<T>> queryForInstances(String name) throws Exception {
        // 直接根据name进行过滤ServiceCache中的缓存数据
        return serviceCache.getInstances().stream()
                .filter(s -> s.getName().equals(name))
                .collect(Collectors.toList());
    }
}

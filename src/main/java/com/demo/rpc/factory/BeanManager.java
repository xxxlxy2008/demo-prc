package com.demo.rpc.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2020-06-21
 */
public class BeanManager {

    private static Map<String, Object> services = new ConcurrentHashMap<>();

    public static void registerBean(String serviceName, Object bean) {
        services.put(serviceName, bean);
    }

    public static Object getBean(String serviceName) {
        return services.get(serviceName);
    }
}
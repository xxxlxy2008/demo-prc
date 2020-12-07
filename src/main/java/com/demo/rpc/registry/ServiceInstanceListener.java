package com.demo.rpc.registry;

import org.apache.curator.x.discovery.ServiceInstance;

public interface ServiceInstanceListener<T> {

    void onRegister(ServiceInstance<T> serviceInstance);

    void onRemove(ServiceInstance<T> serviceInstance);

    void onUpdate(ServiceInstance<T> serviceInstance);

    void onFresh(ServiceInstance<T> serviceInstance, ServerInfoEvent event);

    enum ServerInfoEvent {
        ON_REGISTER,
        ON_UPDATE,
        ON_REMOVE
    }

}

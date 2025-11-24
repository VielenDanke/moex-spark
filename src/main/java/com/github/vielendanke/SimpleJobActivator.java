package com.github.vielendanke;

import org.jobrunr.server.JobActivator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleJobActivator implements JobActivator {
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public SimpleJobActivator(Object... serviceInstances) {
        for (Object service : serviceInstances) {
            services.put(service.getClass(), service);
        }
    }

    @Override
    public <T> T activateJob(Class<T> type) {
        if (!services.containsKey(type)) {
            throw new RuntimeException("Service not found: " + type.getName());
        }
        return type.cast(services.get(type));
    }
}

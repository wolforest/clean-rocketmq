package com.wolf.minimq.broker;

import com.wolf.common.convention.container.ApplicationContext;

/**
 * broker context, A very simple IOC container
 */
public class BrokerContext {
    private static final ApplicationContext INSTANCE = new ApplicationContext();
    private static final ApplicationContext API = new ApplicationContext();

    public static void register(Object bean) {
        INSTANCE.register(bean);
    }

    public static void register(Object bean, Class<?> beanClass) {
        INSTANCE.register(bean, beanClass);
    }

    public static <T> T getBean(Class<T> beanClass) {
        return INSTANCE.getBean(beanClass);
    }

    public static void registerAPI(Object bean) {
        API.register(bean);
    }

    public static void registerAPI(Object bean, Class<?> beanClass) {
        API.register(bean, beanClass);
    }

    public static <T> T getAPI(Class<T> beanClass) {
        return API.getBean(beanClass);
    }
}

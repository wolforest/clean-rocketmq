package cn.coderule.minimq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;

/**
 * broker bootstrap, A very simple IOC container
 */
public class BrokerContext {
    public static final ApplicationContext APPLICATION = new ApplicationContext();
    public static final ApplicationContext API = new ApplicationContext();
    public static final ApplicationContext MONITOR = new ApplicationContext();

    public static void registerContext(ApplicationContext context) {
        APPLICATION.registerContext(context);
    }

    public static void register(Object bean) {
        APPLICATION.register(bean);
    }

    public static void register(Object bean, Class<?> beanClass) {
        APPLICATION.register(bean, beanClass);
    }

    public static <T> T getBean(Class<T> beanClass) {
        return APPLICATION.getBean(beanClass);
    }

    public static <T> T getBean(Class<T> beanClass, boolean throwNotFoundException) {
        return APPLICATION.getBean(beanClass, throwNotFoundException);
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

    public static void registerMonitor(Object bean) {
        MONITOR.register(bean);
    }

    public static void registerMonitor(Object bean, Class<?> beanClass) {
        MONITOR.register(bean, beanClass);
    }

    public static <T> T getMonitor(Class<T> beanClass) {
        return MONITOR.getBean(beanClass);
    }

}

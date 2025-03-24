package cn.coderule.minimq.registry.server.context;

import cn.coderule.common.convention.container.ApplicationContext;

/**
 * Registry bootstrap, A very simple IOC container
 */
public class RegistryContext {
    public static final ApplicationContext APPLICATION = new ApplicationContext();
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

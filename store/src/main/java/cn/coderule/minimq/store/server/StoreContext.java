package cn.coderule.minimq.store.server;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.minimq.store.server.bootstrap.StoreScheduler;
import lombok.extern.slf4j.Slf4j;

/**
 * store bootstrap, A very simple IOC container
 */
@Slf4j
public class StoreContext {
    public static final ApplicationContext APPLICATION = new ApplicationContext();
    public static final ApplicationContext API = new ApplicationContext();
    public static ApplicationContext MONITOR = new ApplicationContext();

    public static StoreCheckpoint CHECK_POINT;
    public static StoreScheduler SCHEDULER;

    public static void setScheduler(StoreScheduler scheduler) {
        SCHEDULER = scheduler;
    }

    public static StoreScheduler getScheduler() {
        if (SCHEDULER == null) {
            throw new RuntimeException("scheduler is null");
        }
        return SCHEDULER;
    }

    public static void setCheckPoint(StoreCheckpoint checkPoint) {
        if (CHECK_POINT != null) {
            throw new RuntimeException("checkPoint is not null");
        }
        CHECK_POINT = checkPoint;
    }

    public static StoreCheckpoint getCheckPoint() {
        return CHECK_POINT;
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

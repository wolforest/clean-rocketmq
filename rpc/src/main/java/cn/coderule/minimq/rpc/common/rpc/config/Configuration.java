
package cn.coderule.minimq.rpc.common.rpc.config;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.bean.BeanUtil;
import cn.coderule.minimq.domain.domain.meta.DataVersion;
import com.alibaba.fastjson2.JSON;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Configuration {
    private final List<Object> configObjectList = new ArrayList<>(4);
    private String storePath;
    private boolean storePathFromConfig = false;
    private Object storePathObject;
    private Field storePathField;
    private final DataVersion dataVersion = new DataVersion();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * All properties include configs in object and extend properties.
     */
    private final Properties allConfigs = new Properties();

    public Configuration(Object... configObjects) {
        if (configObjects == null) {
            return;
        }
        for (Object configObject : configObjects) {
            if (configObject == null) {
                continue;
            }
            registerConfig(configObject);
        }
    }

    public Configuration(String storePath, Object... configObjects) {
        this(configObjects);
        this.storePath = storePath;
    }

    /**
     * register config object
     *
     * @return the current Configuration object
     */
    public Configuration registerConfig(Object configObject) {
        try {
            readWriteLock.writeLock().lockInterruptibly();

            try {

                Properties registerProps = BeanUtil.toProperties(configObject);

                merge(registerProps, this.allConfigs);

                configObjectList.add(configObject);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("registerConfig lock error");
        }
        return this;
    }

    /**
     * register config properties
     *
     * @return the current Configuration object
     */
    public Configuration registerConfig(Properties extProperties) {
        if (extProperties == null) {
            return this;
        }

        try {
            readWriteLock.writeLock().lockInterruptibly();

            try {
                merge(extProperties, this.allConfigs);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("register lock error. {}" + extProperties);
        }

        return this;
    }

    /**
     * The store path will be gotten from the field of object.
     *
     * @throws RuntimeException if the field of object is not exist.
     */
    public void setStorePathFromConfig(Object object, String fieldName) {
        assert object != null;

        try {
            readWriteLock.writeLock().lockInterruptibly();

            try {
                this.storePathFromConfig = true;
                this.storePathObject = object;
                // check
                this.storePathField = object.getClass().getDeclaredField(fieldName);
                assert this.storePathField != null
                    && !Modifier.isStatic(this.storePathField.getModifiers());
                this.storePathField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("setStorePathFromConfig lock error");
        }
    }

    private String getStorePath() {
        String realStorePath = null;
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {
                realStorePath = this.storePath;

                if (this.storePathFromConfig) {
                    try {
                        realStorePath = (String) storePathField.get(this.storePathObject);
                    } catch (IllegalAccessException e) {
                        log.error("getStorePath error, ", e);
                    }
                }
            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getStorePath lock error");
        }

        return realStorePath;
    }

    public void setStorePath(final String storePath) {
        this.storePath = storePath;
    }

    public void update(Properties properties) {
        try {
            readWriteLock.writeLock().lockInterruptibly();

            try {
                // the property must exist when update
                mergeIfExist(properties, this.allConfigs);

                for (Object configObject : configObjectList) {
                    // not allConfigs to update...
                    BeanUtil.toObject(properties, configObject);
                }

                this.dataVersion.nextVersion();

            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("update lock error, {}", properties);
            return;
        }

        persist();
    }

    public void persist() {
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {
                String allConfigs = getAllConfigsInternal();

                FileUtil.stringToFile(allConfigs, getStorePath());
            } catch (Exception e) {
                log.error("persist string2File error, ", e);
            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("persist lock error");
        }
    }

    public String getAllConfigsFormatString() {
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {

                return getAllConfigsInternal();

            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getAllConfigsFormatString lock error");
        }

        return null;
    }

    public String getClientConfigsFormatString(List<String> clientKeys) {
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {

                return getClientConfigsInternal(clientKeys);

            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getAllConfigsFormatString lock error");
        }

        return null;
    }

    public String getDataVersionJson() {
        return JSON.toJSONString(this.dataVersion);
    }

    public Properties getAllConfigs() {
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {

                return this.allConfigs;

            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getAllConfigs lock error");
        }

        return null;
    }

    private String getAllConfigsInternal() {
        StringBuilder stringBuilder = new StringBuilder();

        // reload from config object ?
        for (Object configObject : this.configObjectList) {
            Properties properties = BeanUtil.toProperties(configObject);
            if (properties != null) {
                merge(properties, this.allConfigs);
            } else {
                log.warn("getAllConfigsInternal object2Properties is null, {}", configObject.getClass());
            }
        }

        {
            stringBuilder.append(BeanUtil.toString(this.allConfigs, true));
        }

        return stringBuilder.toString();
    }

    private String getClientConfigsInternal(List<String> clientConigKeys) {
        StringBuilder stringBuilder = new StringBuilder();
        Properties clientProperties = new Properties();

        // reload from config object ?
        for (Object configObject : this.configObjectList) {
            Properties properties = BeanUtil.toProperties(configObject);

            for (String nameNow : clientConigKeys) {
                if (properties.containsKey(nameNow)) {
                    clientProperties.put(nameNow, properties.get(nameNow));
                }
            }

        }
        stringBuilder.append(BeanUtil.toString(clientProperties));

        return stringBuilder.toString();
    }

    private void merge(Properties from, Properties to) {
        for (Entry<Object, Object> next : from.entrySet()) {
            Object fromObj = next.getValue(), toObj = to.get(next.getKey());
            if (toObj != null && !toObj.equals(fromObj)) {
                log.info("Replace, key: {}, value: {} -> {}", next.getKey(), toObj, fromObj);
            }
            to.put(next.getKey(), fromObj);
        }
    }

    private void mergeIfExist(Properties from, Properties to) {
        for (Entry<Object, Object> next : from.entrySet()) {
            if (!to.containsKey(next.getKey())) {
                continue;
            }

            Object fromObj = next.getValue(), toObj = to.get(next.getKey());
            if (toObj != null && !toObj.equals(fromObj)) {
                log.info("Replace, key: {}, value: {} -> {}", next.getKey(), toObj, fromObj);
            }
            to.put(next.getKey(), fromObj);
        }
    }

}

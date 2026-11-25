package com.wolfmq.common.config.cli;

import com.wolfmq.common.server.registry.RegistryUtils;

public class CliConstants {
    public static final String BROKER_CONFIG_FILE = "broker.yml";
    public static final String STORE_CONFIG_FILE = "store.yml";
    public static final String REGISTRY_CONFIG_FILE = "registry.yml";

    public static String getDefaultRegistry() {
        String registry = System.getProperty(RegistryUtils.NAMESRV_ADDR_PROPERTY);
        if (cn.coderule.common.util.lang.string.StringUtil.notBlank(registry)) {
            return registry;
        }

        registry = System.getenv(RegistryUtils.NAMESRV_ADDR_ENV);
        if (cn.coderule.common.util.lang.string.StringUtil.notBlank(registry)) {
            return registry;
        }

        return  "127.0.0.1:9876";
    }
}

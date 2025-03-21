package cn.coderule.minimq.registry.server.context;

import cn.coderule.minimq.domain.config.RegistryConfig;

public class ConfigLoader {
    public static void load() {
        RegistryContext.register(new RegistryConfig());
    }
}

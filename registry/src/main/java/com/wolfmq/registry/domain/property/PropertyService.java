package com.wolfmq.registry.domain.property;

import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.rpc.common.rpc.config.Configuration;
import com.wolfmq.domain.config.network.RpcServerConfig;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyService {
    private final Configuration configuration;
    private final RegistryConfig registryConfig;

    private final Set<String> configBlackList = new HashSet<>();

    public PropertyService(RegistryConfig registryConfig, RpcServerConfig rpcServerConfig) {
        this.registryConfig = registryConfig;
        this.configuration = new Configuration(registryConfig, rpcServerConfig);

        initConfigBlackList();
    }

    public void update(Properties properties) {
        configuration.update(properties);
    }

    public String getString() {
        return configuration.getAllConfigsFormatString();
    }

    public boolean validateBlackList(Properties properties) {
        for (String blackConfig : configBlackList) {
            if (properties.containsKey(blackConfig)) {
                return true;
            }
        }
        return false;
    }

    private void initConfigBlackList() {
        configBlackList.add("configBlackList");
        configBlackList.add("configStorePath");
        configBlackList.add("kvConfigPath");
        configBlackList.add("rocketmqHome");

        String[] configArray = registryConfig.getConfigBlackList().split(";");
        configBlackList.addAll(Arrays.asList(configArray));
    }


}

package com.wolfmq.common.config.cli;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import lombok.Data;
import picocli.CommandLine.Option;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.bean.BeanUtil;

@Data
public class BrokerArgument implements Serializable {
    @Option(names = {"-p", "--configPath"}, description = "config path")
    private String configPath;

    @Option(names = {"-b", "--brokerConfig"}, description = "broker config file")
    private String brokerConfig;

    @Option(names = {"-s", "--StoreConfig"}, description = "Store config file")
    private String StoreConfig;

    @Option(names = {"-r", "--registry"}, description = "registry address: addr1,addr2,...")
    private String registry;

    @Option(names = {"-m", "--mode"}, description = "running mode")
    private String mode;

    public String getConfigPath() {
        if (StringUtil.notBlank(configPath)) {
            return configPath;
        }

        URL resource = this.getClass().getClassLoader().getResource("");
        if (null == resource) {
            throw new RuntimeException("Unable to find config path");
        }

        configPath = resource.getPath();
        return configPath;
    }


    public String getBrokerConfig() {
        if (StringUtil.notBlank(brokerConfig)) {
            return brokerConfig;
        }

        brokerConfig = getConfigPath()
            + File.separator
            + CliConstants.BROKER_CONFIG_FILE;

        return brokerConfig;
    }

    public String getStoreConfig() {
        if (StringUtil.notBlank(StoreConfig)) {
            return StoreConfig;
        }

        StoreConfig = getConfigPath()
            + File.separator
            + CliConstants.STORE_CONFIG_FILE;

        return StoreConfig;
    }

    public void validate() {

    }

    public StoreArgument toStoreArgument() {
        StoreArgument storeArgument = new StoreArgument();
        BeanUtil.copyProperties(this, storeArgument);
        return storeArgument;
    }
}

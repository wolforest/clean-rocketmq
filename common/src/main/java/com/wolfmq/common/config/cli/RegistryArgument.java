package com.wolfmq.common.config.cli;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import lombok.Data;
import picocli.CommandLine.Option;
import cn.coderule.common.util.lang.string.StringUtil;

@Data
public class RegistryArgument implements Serializable {
    @Option(names = {"-p", "--configPath"}, description = "config path")
    private String configPath;

    @Option(names = {"-r", "--registryConfig"}, description = "registry config file")
    private String registryConfig;


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

    public String getRegistryConfig() {
        if (StringUtil.notBlank(registryConfig)) {
            return registryConfig;
        }

        registryConfig = getConfigPath()
            + File.separator
            + CliConstants.REGISTRY_CONFIG_FILE;

        return registryConfig;
    }

    public void validate() {

    }
}

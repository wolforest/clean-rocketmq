package com.wolfmq.domain.config.cli;

import cn.coderule.common.util.lang.bean.YamlUtil;
import com.wolfmq.domain.config.base.CommonConverter;
import com.wolfmq.domain.config.server.RegistryConfig;
import java.io.IOException;
import lombok.Getter;
import picocli.CommandLine;

public class RegistryConfigLoader {
    private final String[] args;

    @Getter
    private RegistryArgument argument;
    @Getter
    private RegistryConfig config;

    public RegistryConfigLoader(String[] args) {
        this.args = args;
    }

    public RegistryConfig load() throws IOException {
        this.parseArgs();
        config = YamlUtil.load(argument.getConfigFile(), RegistryConfig.class);

        if (null == config) {
            throw new IllegalArgumentException("Fail to parse Config file");
        }

        if (null != config.getCommon()) {
            CommonConverter.convert(config.getCommon());
        }

        return config;
    }

    private void parseArgs() {
        if (null != argument) return;

        argument = new RegistryArgument();

        CommandLine cmd = new CommandLine(argument);
        cmd.setExecutionExceptionHandler(
            (ex, commandLine, parseResult) -> {
                throw new IllegalArgumentException(ex.getMessage());
            }
        );

        cmd.parseArgs(args);
        argument.validate();
    }
}

package com.wolfmq.domain.config.cli;

import cn.coderule.common.util.lang.bean.YamlUtil;
import com.wolfmq.domain.config.base.CommonConverter;
import com.wolfmq.domain.config.server.StoreConfig;
import java.io.IOException;
import lombok.Getter;
import picocli.CommandLine;

public class StoreConfigLoader {
    private final String[] args;

    @Getter
    private StoreArgument argument;
    @Getter
    private StoreConfig config;

    public StoreConfigLoader(String[] args) {
        this.args = args;
    }

    public StoreConfig load() throws IOException {
        this.parseArgs();
        config = YamlUtil.load(argument.getConfigFile(), StoreConfig.class);

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

        argument = new StoreArgument();

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

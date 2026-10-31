package com.wolfmq.domain.config.cli;

import java.io.Serializable;
import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class BrokerArgument implements Serializable {
    @Option(names = {"-c", "--configFile"}, description = "config file")
    private String configFile;

    @Option(names = {"-m", "--mode"}, description = "running mode")
    private String mode;

    public void validate() {

    }
}

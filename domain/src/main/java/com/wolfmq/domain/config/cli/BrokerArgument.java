package com.wolfmq.domain.config.cli;

import java.io.Serializable;
import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class BrokerArgument implements Serializable {
    @Option(names = {"-c", "--configFile"}, description = "config file", required = true)
    private String configFile;


    public void validate() {

    }
}

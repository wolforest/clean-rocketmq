package com.wolfmq.domain.config.cli;

import java.io.Serializable;
import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class RegistryArgument implements Serializable {
    @Option(names = {"-c", "--configFile"}, description = "config file", required = true)
    private String configFile;

    @Option(names = {"-m", "--mode"}, description = "running mode")
    private String mode;

    @Option(names = {"-r", "--registry"}, description = "registry address: addr1,addr2,...")
    private String registry;

    public void validate() {

    }
}

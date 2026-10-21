package cn.coderule.wolfmq.domain.config.cli;

import java.io.Serializable;
import lombok.Data;
import picocli.CommandLine;

@Data
public class RegistryArgument implements Serializable {
    @CommandLine.Option(names = {"-c", "--configFile"}, description = "config file", required = true)
    private String configFile;

    public void validate() {

    }
}

package cn.coderule.wolfmq.domain.config.template;

import java.io.Serializable;
import lombok.Data;

@Data
public class Production implements Serializable {
    private int mmapFileSize = 1024 * 1024 * 1024;
}

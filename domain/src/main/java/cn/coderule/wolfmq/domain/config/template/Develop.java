package cn.coderule.wolfmq.domain.config.template;

import java.io.Serializable;
import lombok.Data;

@Data
public class Develop implements Serializable {
    private int mmapFileSize = 10 * 1024;
}

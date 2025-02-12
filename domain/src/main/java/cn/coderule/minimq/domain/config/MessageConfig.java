package cn.coderule.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class MessageConfig implements Serializable {
    private int maxSize;
    private int maxBodySize;

    private int maxGetSize;
}

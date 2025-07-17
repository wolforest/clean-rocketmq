package cn.coderule.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class TimerConfig implements Serializable {
    private boolean enableTimer = true;
    private boolean enableRocksDB = false;
}

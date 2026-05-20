package cn.coderule.wolfmq.domain.config.store;

import java.io.Serializable;
import lombok.Data;

@Data
public class IndexConfig implements Serializable {
    private int maxHashSlotNum = 500_000;
    private int maxIndexNum = 20_000_000;

    private int minFlushPages = 1;
    private int flushInterval = 1000 * 10;
}
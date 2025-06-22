package cn.coderule.minimq.domain.config.store;

import java.io.Serializable;
import lombok.Data;

@Data
public class ConsumeQueueConfig implements Serializable {
    /**
     * set while Store initialized
     */
    private String rootPath;

    private int unitSize = 20;
    // fileSize = unitNum * unitSize
    private int fileSize = 300_000 * 20;

    private int minFlushPages = 2;
    private int flushInterval = 1000;
    private int flushAllInterval = 1000 * 60;

    private int maxEnqueueRetry = 30;


}

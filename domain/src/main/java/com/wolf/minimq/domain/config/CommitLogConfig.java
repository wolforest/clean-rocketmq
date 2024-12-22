package com.wolf.minimq.domain.config;

import com.wolf.minimq.domain.enums.FlushType;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommitLogConfig implements Serializable {
    private boolean enableMultiPath = false;
    private boolean enableWriteCache = false;

    private String dir = "commitlog";
    private int fileSize = 100 * 1024 * 1024;

    private FlushType flushType = FlushType.ASYNC;

}

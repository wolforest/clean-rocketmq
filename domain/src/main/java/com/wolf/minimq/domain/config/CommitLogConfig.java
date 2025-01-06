package com.wolf.minimq.domain.config;

import com.wolf.minimq.domain.enums.FlushType;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommitLogConfig implements Serializable {
    private boolean enableMultiPath = false;
    private boolean enableWriteCache = false;

    private FlushType flushType = FlushType.ASYNC;

    private String dirName = "commitlog";
    private int fileSize = 100 * 1024 * 1024;

    private int minFlushPages = 4;
    private int flushInterval = 200;
    private int flushAllInterval = 1000 * 10;

    private int minCommitPages = 4;
    private int commitInterval = 200;
    private int commitAllInterval = 200;

}

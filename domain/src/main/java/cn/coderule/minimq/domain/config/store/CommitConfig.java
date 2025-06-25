package cn.coderule.minimq.domain.config.store;

import cn.coderule.minimq.domain.domain.core.enums.store.FlushType;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommitConfig implements Serializable {
    private boolean enableMultiPath = false;
    private boolean enableWriteCache = false;

    private FlushType flushType = FlushType.ASYNC;

    private String dirName = "commitlog";
    private int fileSize = 100 * 1024 * 1024;

    private boolean enableFlushSleep = true;
    private int minFlushPages = 4;
    private int flushInterval = 500;
    private int flushTimeout = 1000 * 5;
    private int ThroughFlushInterval = 1000 * 10;

    private int minCommitPages = 4;
    private int commitInterval = 200;
    private int ThroughCommitInterval = 200;

}

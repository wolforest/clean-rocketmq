package cn.coderule.minimq.domain.config.store;

import cn.coderule.minimq.domain.config.ConfigAttribute;
import cn.coderule.minimq.domain.core.enums.store.FlushType;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommitConfig implements Serializable {
    private boolean enableMultiPath = false;
    private boolean enableWriteCache = false;

    private FlushType flushType = ConfigAttribute.FLUSH_TYPE;

    private String dirName = "commitlog";
    private int fileSize = ConfigAttribute.MMAP_FILE_SIZE;

    private boolean enableFlushSleep = true;
    private int minFlushPages = 4;
    private int flushInterval = 500;
    private int flushTimeout = 1000 * 5;
    private int ThroughFlushInterval = 1000 * 10;

    private int minCommitPages = 4;
    private int commitInterval = 200;
    private int ThroughCommitInterval = 200;

}

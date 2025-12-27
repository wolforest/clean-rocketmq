package cn.coderule.minimq.domain.config.store;

import cn.coderule.minimq.domain.config.ConfigAttribute;
import cn.coderule.minimq.domain.core.enums.store.FlushType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CommitConfig implements Serializable {
    private boolean enableMultiDir = false;
    private boolean enableWriteCache = false;

    private boolean enableSharding = false;
    private int maxShardingNumber = 10;

    private String dirName = "commitlog";
    private List<String> dirList = new ArrayList<>();

    private int fileSize = ConfigAttribute.MMAP_FILE_SIZE;

    private boolean enableFlushSleep = true;
    private FlushType flushType = ConfigAttribute.FLUSH_TYPE;
    private int minFlushPages = 4;
    private int flushInterval = 500;
    private int flushTimeout = 5_000;
    private int ThroughFlushInterval = 10_000;

    private int minCommitPages = 4;
    private int commitInterval = 200;
    private int ThroughCommitInterval = 200;

}

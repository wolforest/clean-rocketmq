package com.wolfmq.common.domain.meta.offset;

import com.wolfmq.common.domain.meta.DataVersion;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

@Data
public class TagOffset implements Serializable {
    public static final String TOPIC_GROUP_SEPARATOR = "@";

    /**
     * topic@group -> tag -> offset
     */
    private ConcurrentMap<String, ConcurrentMap<String, Long>> offsetTable;

    private DataVersion dataVersion;
    private final transient AtomicLong versionCounter;

    public TagOffset() {
        offsetTable = new ConcurrentHashMap<>();

        dataVersion = new DataVersion();
        versionCounter = new AtomicLong(0);
    }
}

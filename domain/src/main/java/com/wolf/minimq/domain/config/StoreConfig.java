package com.wolf.minimq.domain.config;

import java.io.File;
import java.io.Serializable;
import lombok.Data;

@Data
public class StoreConfig implements Serializable {
    private int syncFlushTimeout = 5 * 1000;
    private String rootDir = System.getProperty("user.home") + File.separator + "store";

    private boolean enableTransientPool = false;
    private int transientPoolSize = 5;
    private int transientFileSize = 100 * 1024 * 1024;

}

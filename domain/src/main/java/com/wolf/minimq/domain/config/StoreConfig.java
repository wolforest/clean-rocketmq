package com.wolf.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class StoreConfig implements Serializable {
    private int syncFlushTimeout = 5 * 1000;
}

package com.wolfmq.domain.config.base.mode;

import com.wolfmq.domain.config.base.Common;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Slave extends Common implements Serializable {
    protected Integer mmapFileSize = 1024 * 1024 * 1024;
}

package com.wolfmq.domain.config.base.mode;

import com.wolfmq.domain.config.base.Common;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Develop extends Common implements Serializable {
    private Integer mmapFileSize = 10 * 1024;
}

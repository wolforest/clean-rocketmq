package cn.coderule.minimq.domain.domain.meta.offset;

import java.io.Serializable;
import lombok.Data;

@Data
public class OffsetResult implements Serializable {
    private long offset;
}

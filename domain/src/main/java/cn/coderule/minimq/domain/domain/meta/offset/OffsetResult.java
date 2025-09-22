package cn.coderule.minimq.domain.domain.meta.offset;

import java.io.Serializable;
import lombok.Data;

@Data
public class OffsetResult implements Serializable {
    private long offset;

    public static OffsetResult build(long offset) {
        OffsetResult result = new OffsetResult();
        result.setOffset(offset);
        return result;
    }

    public static OffsetResult notFound() {
        return build(-1);
    }

    public boolean isSuccess() {
        return offset >= 0;
    }
}

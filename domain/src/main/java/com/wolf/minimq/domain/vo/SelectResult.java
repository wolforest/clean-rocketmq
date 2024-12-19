package com.wolf.minimq.domain.vo;

import com.wolf.minimq.domain.enums.AppendStatus;
import com.wolf.minimq.domain.service.store.MappedFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectResult implements Serializable {
    private long startOffset;
    private ByteBuffer byteBuffer;
    private int size;
    protected MappedFile mappedFile;
    private boolean isInCache = true;
}

package com.wolfmq.store.domain.commitlog.vo;

import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.core.mmap.MappedFile;
import com.wolfmq.domain.domain.message.codec.MessageEncoder;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertContext implements Serializable {
    private long now;
    private MessageBO messageBO;
    @Builder.Default
    private EnqueueResult result = null;
    @Builder.Default
    private long elapsedTimeInLock = 0;
    private MappedFile mappedFile;

    private MessageEncoder encoder;
}

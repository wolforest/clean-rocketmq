package cn.coderule.minimq.domain.domain.dto;

import cn.coderule.minimq.domain.service.store.domain.mq.MessageFilter;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DequeueRequest implements Serializable {
    private String group;
    private String topic;
    private int queueId;
    private long offset;

    @Builder.Default
    private int num = 1;
    private int maxSize;

    @Builder.Default
    private MessageFilter filter = null;
}


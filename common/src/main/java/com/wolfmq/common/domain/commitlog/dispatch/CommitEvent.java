package com.wolfmq.common.domain.commitlog.dispatch;

import com.wolfmq.common.domain.message.MessageBO;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommitEvent implements Serializable {
    private MessageBO messageBO;

    // filter info
    private byte[] bitMap;

    public static CommitEvent of(MessageBO messageBO) {
        return CommitEvent.builder()
            .messageBO(messageBO)
            .build();
    }
}

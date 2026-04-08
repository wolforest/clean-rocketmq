package cn.coderule.wolfmq.store.domain.commitlog.vo;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFile;
import cn.coderule.wolfmq.domain.domain.message.MessageEncoder;
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

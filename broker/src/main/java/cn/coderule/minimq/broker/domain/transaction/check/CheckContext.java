package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.minimq.domain.config.TransactionConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckContext implements Serializable {
    private TransactionContext transactionContext;
    private TransactionConfig transactionConfig;

    private MessageQueue prepareQueue;
    private MessageQueue commitQueue;

    private long prepareOffset;
    private long prepareNextOffset;
    private long prepareMessageCount;

    private long commitOffset;
    private long commitNextOffset;

    @Builder.Default
    private long startTime = System.currentTimeMillis();
    // commitOffset list
    @Builder.Default
    private List<Long> commitOffsetList = new ArrayList<>();
    // prepareOffset -> commitOffset
    @Builder.Default
    private Map<Long, Long> offsetMap = new HashMap<>();
    // commitOffset -> Set<PrepareOffset>
    @Builder.Default
    private Map<Long, Set<Long>> commitOffsetMap = new HashMap<>();

    @Builder.Default
    private int invalidMessageCount = 1;
    // count of renewed prepare message
    @Builder.Default
    private int renewMessageCount = 0;
    @Builder.Default
    private int rpcFailureCount = 0;

    public boolean isOffsetValid() {
        return prepareOffset >= 0 && commitOffset >= 0;
    }

    public void initOffset(long commitNextOffset) {
        this.prepareNextOffset = prepareOffset;
        this.prepareMessageCount = prepareOffset;
        this.commitNextOffset = commitNextOffset;
    }

    public boolean isTimeout(long maxTime) {
        long elapsedTime = System.currentTimeMillis() - this.startTime;
        return elapsedTime > maxTime;
    }
}

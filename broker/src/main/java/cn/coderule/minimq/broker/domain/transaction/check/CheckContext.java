package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.minimq.domain.domain.MessageQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class CheckContext implements Serializable {
    private long startTime = System.currentTimeMillis();

    private MessageQueue prepareQueue;
    private MessageQueue commitQueue;

    private long prepareOffset;
    private long prepareNextOffset;
    private long prepareMessageCount;

    private long commitOffset;
    private long commitNextOffset;

    // commitOffset list
    private List<Long> commitOffsetList = new ArrayList<>();
    // prepareOffset -> commitOffset
    private Map<Long, Long> offsetMap = new HashMap<>();
    // commitOffset -> Set<PrepareOffset>
    private Map<Long, Set<Long>> commitOffsetMap = new HashMap<>();

    private int invalidMessageCount = 1;
    // count of renewed prepare message
    private int renewMessageCount = 0;
    private int rpcFailureCount = 0;
}

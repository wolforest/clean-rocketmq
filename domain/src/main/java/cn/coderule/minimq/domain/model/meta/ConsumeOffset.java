package cn.coderule.minimq.domain.model.meta;

import com.alibaba.fastjson2.annotation.JSONField;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;

@Data
public class ConsumeOffset implements Serializable {

    private ConcurrentMap<String, ConcurrentMap<Integer, Long>> offsetMap
        = new ConcurrentHashMap<>(512);

    @JSONField(serialize = false)
    public long getOffset(String group, String topic, int queueId) {
        return 0;
    }

    @JSONField(serialize = false)
    public long getAndRemove(String group, String topic, int queueId) {
        return 0;
    }

    @JSONField(serialize = false)
    public Map<Integer, Long> getAll(String group, String topic) {
        return null;
    }

    @JSONField(serialize = false)
    public void putOffset(String group, String topic, int queueId, long offset) {

    }

}

package cn.coderule.minimq.domain.domain.meta.offset;

import java.io.Serializable;
import java.util.Set;
import lombok.Data;

@Data
public class TopicResult implements Serializable {
    private Set<String> topicSet;

    public static TopicResult build(Set<String> topicSet) {
        TopicResult result = new TopicResult();
        result.topicSet = topicSet;
        return result;
    }

}

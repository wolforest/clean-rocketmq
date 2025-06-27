package cn.coderule.minimq.domain.domain.meta.offset;

import java.io.Serializable;
import java.util.Set;
import lombok.Data;

@Data
public class GroupResult implements Serializable {
    private Set<String> groupSet;

    public static GroupResult build(Set<String> topicSet) {
        GroupResult result = new GroupResult();
        result.groupSet = topicSet;
        return result;
    }

}

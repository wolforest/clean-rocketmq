package cn.coderule.minimq.domain.domain.meta.offset;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class OffsetFilter implements Serializable {
    private RequestContext context;
    private String group;
    private String topic;
}

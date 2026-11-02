package com.wolfmq.common.domain.meta.offset;

import com.wolfmq.common.server.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class OffsetFilter implements Serializable {
    private RequestContext context;
    private String group;
    private String topic;
}

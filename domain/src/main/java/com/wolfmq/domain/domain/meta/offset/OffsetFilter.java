package com.wolfmq.domain.domain.meta.offset;

import com.wolfmq.domain.server.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class OffsetFilter implements Serializable {
    private RequestContext context;
    private String group;
    private String topic;
}

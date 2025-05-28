package cn.coderule.minimq.domain.domain.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetRequest implements Serializable {
    private String group;
    private String topic;
    private int queueId;
    private long offset;

    @Builder.Default
    private int num = 1;
    private int maxSize;

    private Object filter;
}


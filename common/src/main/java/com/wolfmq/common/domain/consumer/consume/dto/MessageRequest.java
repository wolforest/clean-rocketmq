package com.wolfmq.common.domain.consumer.consume.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest implements Serializable {
    private String storeGroup;

    private long offset;
    private int size;
}


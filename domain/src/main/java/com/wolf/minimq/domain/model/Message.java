package com.wolf.minimq.domain.model;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private String topic;
    private int flag;
    private Map<String, String> properties;
    private byte[] body;
    private String transactionId;
}

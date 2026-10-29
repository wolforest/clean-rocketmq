package com.wolfmq.domain.server.store;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class StoreState implements Serializable {
    private boolean isolated = false;
    private long stateMachineVersion = 0L;
}

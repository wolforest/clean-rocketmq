package com.wolfmq.common.server.store;

import java.io.Serializable;
import lombok.Data;

@Data
public class StoreState implements Serializable {
    private boolean isolated = false;
    private boolean shutdownSuccessfully = false;
    private long stateMachineVersion = 0L;
}


package com.wolfmq.domain.core.event;

import lombok.Getter;

@Getter
public enum MonitorEvent {
    CONSUMER_LAG,
    TOO_MANY_INFLIGHT_MESSAGES,
    MASTER_TIMEOUT,



}

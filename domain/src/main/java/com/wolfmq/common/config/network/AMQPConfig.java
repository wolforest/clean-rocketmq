package com.wolfmq.common.config.network;

import com.wolfmq.common.config.base.ConfigAttribute;
import java.io.Serializable;
import lombok.Data;

@Data
public class AMQPConfig implements Serializable {
    private int port = ConfigAttribute.AMQP_PORT;

    private boolean enableAmqp10 = true;
    private boolean enableAmqp091 = false;

}

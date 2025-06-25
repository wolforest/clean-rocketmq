package cn.coderule.minimq.rpc.common.core.relay.request;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import lombok.Data;

@Data
public class TransactionRequest implements Serializable {
    private MessageBO messageBO;
}

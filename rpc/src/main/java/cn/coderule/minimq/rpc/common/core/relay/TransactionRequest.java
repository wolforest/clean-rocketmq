package cn.coderule.minimq.rpc.common.core.relay;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import java.io.Serializable;
import lombok.Data;

@Data
public class TransactionRequest implements Serializable {
    private MessageBO messageBO;
}

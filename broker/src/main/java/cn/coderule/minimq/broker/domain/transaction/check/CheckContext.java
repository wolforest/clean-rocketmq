package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckContext implements Serializable {
    private BrokerConfig brokerConfig;
    private MessageService messageService;

}

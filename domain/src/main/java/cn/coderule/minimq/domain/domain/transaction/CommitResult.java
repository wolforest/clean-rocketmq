package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommitResult implements Serializable {
    private int responseCode;
    private String responseMessage;

    private MessageBO messageBO;
}

package cn.coderule.minimq.domain.domain.consumer.ack.broker;

import cn.coderule.minimq.domain.core.enums.consume.AckStatus;
import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AckResult implements Serializable {
    private AckStatus status;
    private String extraInfo = "";
    private String receiptStr = "";
    private long popTime;

    public static AckResult success() {
        AckResult result = new AckResult();
        result.setStatus(AckStatus.OK);

        return result;
    }

    public boolean isSuccess() {
        return status == AckStatus.OK;
    }

}

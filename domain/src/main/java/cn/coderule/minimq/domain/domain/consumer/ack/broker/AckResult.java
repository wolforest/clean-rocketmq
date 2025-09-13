package cn.coderule.minimq.domain.domain.consumer.ack.broker;

import cn.coderule.minimq.domain.core.enums.consume.AckStatus;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AckResult implements Serializable {
    private AckStatus status;
    private String extraInfo = "";
    private String receiptStr = "";

    private long popTime;
    private long invisibleTime;
    private long reviveQueueId;
    private long commitOffset;

    public static AckResult success() {
        AckResult result = new AckResult();
        result.setStatus(AckStatus.OK);

        return result;
    }

    public static AckResult failure() {
        AckResult result = new AckResult();
        result.setStatus(AckStatus.NO_EXIST);

        return result;
    }

    public boolean isSuccess() {
        return status == AckStatus.OK;
    }

    public AckResult appendCheckpointFailure() {
        this.status  = AckStatus.NO_EXIST;

        return this;
    }

}

package cn.coderule.minimq.domain.domain.consumer.ack.broker;

import cn.coderule.minimq.domain.core.enums.consume.AckStatus;
import cn.coderule.minimq.domain.core.exception.BrokerException;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageIdReceipt;

public class AckResult {
    private AckStatus status;
    private String extraInfo;
    private long popTime;

    private MessageIdReceipt idReceipt;
    private BrokerException brokerException;

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    public long getPopTime() {
        return popTime;
    }

    public AckStatus getStatus() {
        return status;
    }

    public void setStatus(AckStatus status) {
        this.status = status;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    @Override
    public String toString() {
        return "AckResult [AckStatus=" + status + ",extraInfo=" + extraInfo + "]";
    }
}

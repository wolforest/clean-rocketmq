package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.core.enums.consume.PopStatus;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.util.List;

public class PopConverter {
    public static PopResult toPopResult(PopContext context, DequeueResult dequeueResult,  PopResult lastResult) {
        if (dequeueResult.isEmpty()) {
            return lastResult;
        }

        List<MessageBO> messageList = lastResult.getMessageList();
        messageList.addAll(dequeueResult.getMessageList());

        return PopResult.builder()
            .restNum(1)
            .popStatus(toPopStatus(dequeueResult.getStatus(), lastResult.getPopStatus()))
            .messageList(messageList)
            .popTime(context.getPopTime())
            .invisibleTime(context.getRequest().getInvisibleTime())
            .build();
    }

    public static PopStatus toPopStatus(MessageStatus status, PopStatus lastStatus) {
        PopStatus newStatus = switch (status) {
            case FOUND:
                yield PopStatus.FOUND;
            case OFFSET_TOO_SMALL:
            case OFFSET_OVERFLOW_ONE:
            case OFFSET_OVERFLOW_BADLY:
                yield PopStatus.NO_NEW_MSG;
            case OFFSET_FOUND_NULL:
            case NO_MATCHED_MESSAGE:
            case OFFSET_RESET:
            case MESSAGE_WAS_REMOVING:
            default:
                yield PopStatus.POLLING_NOT_FOUND;
        };

        if (newStatus != PopStatus.FOUND) {
            return lastStatus;
        }

        return newStatus;
    }

}

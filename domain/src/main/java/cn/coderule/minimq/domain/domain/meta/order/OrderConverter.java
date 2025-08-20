package cn.coderule.minimq.domain.domain.meta.order;

public class OrderConverter {
    public static OrderInfo toOrderInfo(OrderRequest request) {
        return OrderInfo.builder()
            .attemptId(request.getAttemptId())
            .popTime(request.getPopTime())
            .invisibleTime(request.getInvisibleTime())
            .offsetList(OrderUtils.buildOffsetList(request.getOffsetList()))
            .lastConsumeTimestamp(System.currentTimeMillis())
            .commitOffsetBit(0)
            .build();
    }
}

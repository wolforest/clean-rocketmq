package com.wolf.minimq.domain.utils;

import com.wolf.minimq.domain.enums.MessageVersion;

public class MessageUtils {
    public static int calculateMessageLength(MessageVersion messageVersion,
        int bodyLength, int topicLength, int propertiesLength) {

        int bornHostLength = 8;
        int storeHostAddressLength = 8;

        return 4 //TOTAL_SIZE
            + 4 //MAGIC_CODE
            + 4 //BODY_CRC
            + 4 //QUEUE_ID
            + 4 //FLAG
            + 8 //QUEUE_OFFSET
            + 8 //COMMITLOG_OFFSET
            + 4 //SYSFLAG
            + 8 //BORN_TIMESTAMP
            + bornHostLength //BORN_HOST
            + 8 //STORE_TIMESTAMP
            + storeHostAddressLength //STORE_HOST_ADDRESS
            + 4 //RECONSUME_TIMES
            + 8 //Prepared Transaction Offset
            + 4 + (Math.max(bodyLength, 0)) //BODY
            + messageVersion.getTopicLengthSize() + topicLength //TOPIC
            + 2 + (Math.max(propertiesLength, 0)); //propertiesLength
    }
}

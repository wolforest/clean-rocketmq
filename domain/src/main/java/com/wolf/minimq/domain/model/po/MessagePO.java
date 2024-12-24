package com.wolf.minimq.domain.model.po;

import java.io.Serializable;
import java.net.SocketAddress;
import lombok.Data;

@Data
public class MessagePO implements Serializable {
    private int messageLength;                  // 1
    private int magicCode;                      // 2
    private int bodyCRC;                        // 3
    private int queueId;                        // 4
    private int flag;                           // 5
    private long queueOffset;                   // 6
    private long commitLogOffset = 0;           // 7
    private int sysFlag;                        // 8
    private long bornTimestamp;                 // 9
    private SocketAddress bornHost;             // 10 (8 bites)
    private long storeTimestamp;                // 11
    private SocketAddress storeHost;            // 12 (8 bites)
    private int reconsumeTimes;                 // 13
    private long transactionOffset;             // 14
    private int bodyLength;                     // 15
    private byte[] body;                        //
    private int topicLength;                    // 16 (short for v2; byte for v1)
    private byte[] topic;                       //
    private short propertiesLength;             // 17
    private byte[] properties;                  //
    private int crc32 = 0;                      // 18 crc32





}

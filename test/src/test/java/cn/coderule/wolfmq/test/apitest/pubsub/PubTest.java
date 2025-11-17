/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.coderule.wolfmq.test.apitest.pubsub;

import cn.coderule.wolfmq.test.apitest.ApiBaseTest;
import cn.coderule.wolfmq.test.manager.ClientManager;
import cn.coderule.wolfmq.test.manager.ProducerManager;
import cn.coderule.wolfmq.test.manager.TopicManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {"client"})
public class PubTest extends ApiBaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(PubTest.class);
    private static final String TOPIC = TopicManager.createUniqueTopic();
    private static final String MESSAGE_PREFIX = "MQT_";
    private static final String MESSAGE_BODY = "test message body: ";

    private Producer producer;
    @BeforeMethod
    public void beforeMethod() {
        TopicManager.createTopic(TOPIC);
        this.producer = ProducerManager.buildProducer(TOPIC);
    }

    @AfterMethod
    public void afterMethod() {
        try {
            stopProducer();
            TopicManager.deleteTopic(TOPIC);
        } catch (Exception e) {
            LOG.error("PubSub afterMethod exception: ", e);
        }
    }

    @Test
    public void testSendOk() {
        if (producer == null) {
            return;
        }
        Message message = createMessage();

        try {
            SendReceipt sendReceipt = producer.send(message);
            Assert.assertNotNull(sendReceipt);

            String messageId = sendReceipt.getMessageId().toString();
            Assert.assertNotNull(messageId);
            Assert.assertFalse(messageId.isEmpty());

            LOG.info("pub message: {}", sendReceipt);
        } catch (Throwable t) {
            LOG.error("Failed to send message: ",  t);
        }
    }

    private void stopProducer() throws IOException {
        if (producer == null) {
            return;
        }

        LOG.info("stop producer");
        producer.close();
    }

    private Message createMessage() {
        int i = 0;
        return ClientManager.getProvider()
            .newMessageBuilder()
            .setTopic(TOPIC)
            .setKeys(MESSAGE_PREFIX + i)
            .setBody((MESSAGE_BODY + i).getBytes(StandardCharsets.UTF_8))
            .build();
    }

}

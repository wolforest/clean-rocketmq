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

/*
  $Id: QueueData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package cn.coderule.minimq.rpc.registry.protocol.route;

import cn.coderule.minimq.domain.model.Topic;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class QueueInfo implements Comparable<QueueInfo> {
    private String brokerName;
    private int readQueueNums;
    private int writeQueueNums;
    private int perm;
    private int topicSysFlag;

    public QueueInfo() {

    }

    public static QueueInfo from(String groupName, Topic topic) {
        QueueInfo queueInfo = new QueueInfo();
        queueInfo.setBrokerName(groupName);

        queueInfo.setReadQueueNums(topic.getReadQueueNums());
        queueInfo.setWriteQueueNums(topic.getWriteQueueNums());
        queueInfo.setPerm(topic.getPerm());
        queueInfo.setTopicSysFlag(topic.getTopicSysFlag());

        return queueInfo;
    }

    // Deep copy QueueData
    public QueueInfo(QueueInfo queueInfo) {
        this.brokerName = queueInfo.brokerName;
        this.readQueueNums = queueInfo.readQueueNums;
        this.writeQueueNums = queueInfo.writeQueueNums;
        this.perm = queueInfo.perm;
        this.topicSysFlag = queueInfo.topicSysFlag;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brokerName == null) ? 0 : brokerName.hashCode());
        result = prime * result + perm;
        result = prime * result + readQueueNums;
        result = prime * result + writeQueueNums;
        result = prime * result + topicSysFlag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QueueInfo other = (QueueInfo) obj;
        if (brokerName == null) {
            if (other.brokerName != null)
                return false;
        } else if (!brokerName.equals(other.brokerName))
            return false;
        if (perm != other.perm)
            return false;
        if (readQueueNums != other.readQueueNums)
            return false;
        if (writeQueueNums != other.writeQueueNums)
            return false;
        return topicSysFlag == other.topicSysFlag;
    }

    @Override
    public String toString() {
        return "QueueData [brokerName=" + brokerName + ", readQueueNums=" + readQueueNums
            + ", writeQueueNums=" + writeQueueNums + ", perm=" + perm + ", topicSysFlag=" + topicSysFlag
            + "]";
    }

    @Override
    public int compareTo(QueueInfo o) {
        return this.brokerName.compareTo(o.getBrokerName());
    }

}

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
package cn.coderule.minimq.domain.utils;

import cn.coderule.minimq.domain.domain.enums.message.CleanupPolicy;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.model.meta.topic.TopicAttributes;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class CleanupUtils {
    public static boolean isCompaction(Optional<Topic> Topic) {
        return Objects.equals(CleanupPolicy.COMPACTION, getDeletePolicy(Topic));
    }

    public static CleanupPolicy getDeletePolicy(Optional<Topic> Topic) {
        if (Topic.isEmpty()) {
            return CleanupPolicy.valueOf(TopicAttributes.CLEANUP_POLICY_ATTRIBUTE.getDefaultValue());
        }

        String attributeName = TopicAttributes.CLEANUP_POLICY_ATTRIBUTE.getName();

        Map<String, String> attributes = Topic.get().getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            return CleanupPolicy.valueOf(TopicAttributes.CLEANUP_POLICY_ATTRIBUTE.getDefaultValue());
        }

        if (attributes.containsKey(attributeName)) {
            return CleanupPolicy.valueOf(attributes.get(attributeName));
        } else {
            return CleanupPolicy.valueOf(TopicAttributes.CLEANUP_POLICY_ATTRIBUTE.getDefaultValue());
        }
    }
}

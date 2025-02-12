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
package cn.coderule.minimq.store.domain.commitlog.vo;

import cn.coderule.minimq.domain.model.bo.MessageBO;
import cn.coderule.minimq.domain.model.dto.EnqueueResult;
import cn.coderule.minimq.domain.service.store.infra.MappedFile;
import cn.coderule.minimq.domain.utils.MessageEncoder;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertContext implements Serializable {
    private long now;
    private MessageBO messageBO;
    @Builder.Default
    private EnqueueResult result = null;
    @Builder.Default
    private long elapsedTimeInLock = 0;
    private MappedFile mappedFile;

    private MessageEncoder encoder;
}

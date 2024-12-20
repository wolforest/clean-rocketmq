package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.context.MessageContext;
import com.wolf.minimq.domain.vo.AppendMessageResult;
import com.wolf.minimq.domain.vo.SelectedMappedBuffer;
import java.util.List;

public interface CommitLogDomainService {
    AppendMessageResult append(MessageContext messageContext);

    SelectedMappedBuffer select(long offset, int size);
    SelectedMappedBuffer select(long offset);
    List<SelectedMappedBuffer> selectAll(long offset, int size);


}

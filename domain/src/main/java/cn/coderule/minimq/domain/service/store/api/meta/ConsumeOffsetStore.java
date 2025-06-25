package cn.coderule.minimq.domain.service.store.api.meta;

import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetResult;

public interface ConsumeOffsetStore {
    OffsetResult getOffset(OffsetRequest request);
    OffsetResult getAndRemove(OffsetRequest request);
    void putOffset(OffsetRequest request);
}

package cn.coderule.minimq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.model.meta.ConsumeOffset;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;

public class DefaultConsumeOffsetService implements ConsumeOffsetService {
    private final String storePath;
    private ConsumeOffset consumeOffset;

    public DefaultConsumeOffsetService(String storePath) {
        this.storePath = storePath;
    }

    @Override
    public Long getOffset(String group, String topic, int queueId) {
        return consumeOffset.getOffset(group, topic, queueId);
    }

    @Override
    public Long getAndRemove(String group, String topic, int queueId) {
        return consumeOffset.getAndRemove(group, topic, queueId);
    }

    @Override
    public void putOffset(String group, String topic, int queueId, long offset) {
        consumeOffset.putOffset(group, topic, queueId, offset);
    }

    @Override
    public void load() {
        if (!FileUtil.exists(storePath)) {
            init();
            return;
        }

        String data = FileUtil.fileToString(storePath);
        decode(data);
    }

    @Override
    public void store() {
        String data = JSONUtil.toJSONString(consumeOffset);
        FileUtil.stringToFile(data, storePath);
    }

    private void init() {
        if (consumeOffset != null) {
            return;
        }

        this.consumeOffset = new ConsumeOffset();
    }

    private void decode(String data) {
        if (StringUtil.isBlank(data)) {
            init();
            return;
        }

        this.consumeOffset = JSONUtil.parse(data, ConsumeOffset.class);
    }
}

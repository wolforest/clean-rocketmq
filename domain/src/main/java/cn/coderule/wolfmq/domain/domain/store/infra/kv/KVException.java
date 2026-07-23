package cn.coderule.wolfmq.domain.domain.store.infra.kv;

import cn.coderule.common.lang.exception.SystemException;

public class KVException extends SystemException {
    public KVException() {
        super(500, "KV exception");
    }

    public KVException(String message) {
        super(500, message);
    }

    public KVException(long code, String message) {
        super(code, message);
    }
}

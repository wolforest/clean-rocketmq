
package cn.coderule.minimq.rpc.common.rpc.core.enums;

public enum SerializeType {
    JSON((byte) 0),
    ROCKETMQ((byte) 1);

    private final byte code;

    SerializeType(byte code) {
        this.code = code;
    }

    public static SerializeType valueOf(byte code) {
        for (SerializeType serializeType : SerializeType.values()) {
            if (serializeType.getCode() == code) {
                return serializeType;
            }
        }
        return null;
    }

    public byte getCode() {
        return code;
    }
}

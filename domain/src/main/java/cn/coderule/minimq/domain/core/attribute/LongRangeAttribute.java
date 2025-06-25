package cn.coderule.minimq.domain.core.attribute;

import lombok.Getter;

import static java.lang.String.format;

public class LongRangeAttribute extends Attribute {
    private final long min;
    private final long max;
    @Getter
    private final long defaultValue;

    public LongRangeAttribute(String name, boolean changeable, long min, long max, long defaultValue) {
        super(name, changeable);
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
    }

    @Override
    public void verify(String value) {
        long l = Long.parseLong(value);
        if (l < min || l > max) {
            throw new RuntimeException(format("value is not in range(%d, %d)", min, max));
        }
    }

}

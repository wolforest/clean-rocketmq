package cn.coderule.minimq.domain.utils.attribute;

import static com.google.common.base.Preconditions.checkNotNull;

public class BooleanAttribute extends Attribute {
    private final boolean defaultValue;

    public BooleanAttribute(String name, boolean changeable, boolean defaultValue) {
        super(name, changeable);
        this.defaultValue = defaultValue;
    }

    @Override
    public void verify(String value) {
        checkNotNull(value);

        if (!"false".equalsIgnoreCase(value) && !"true".equalsIgnoreCase(value)) {
            throw new RuntimeException("boolean attribute format is wrong.");
        }
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}

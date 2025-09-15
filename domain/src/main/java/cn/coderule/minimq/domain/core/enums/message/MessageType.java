
package cn.coderule.minimq.domain.core.enums.message;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Getter;

@Getter
public enum MessageType {
    UNSPECIFIED("UNSPECIFIED"),
    NORMAL("NORMAL"),
    FIFO("FIFO"),
    DELAY("DELAY"),
    PREPARE("PREPARE"),
    COMMIT("COMMIT"),
    TRANSACTION("TRANSACTION"),
    MIXED("MIXED"),
    UNKNOWN("UNKNOWN");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public boolean isTransaction() {
        return this == TRANSACTION;
    }

    public static Set<String> typeSet() {
        return Sets.newHashSet(
            UNSPECIFIED.value,
            NORMAL.value,
            FIFO.value,
            DELAY.value,
            PREPARE.value,
            COMMIT.value,
            TRANSACTION.value,
            MIXED.value);
    }
}

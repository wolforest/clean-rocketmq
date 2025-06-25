
package cn.coderule.minimq.domain.domain.core.enums;

import cn.coderule.common.util.lang.string.StringUtil;
import lombok.Getter;

@Getter
public enum RequestType {

    ONEWAY("oneway"),
    SYNC("sync"),
    ASYNC("async");

    private final String name;

    RequestType(String name) {
        this.name = name;
    }

    public static RequestType of(String name) {
        if (StringUtil.isBlank(name)) {
            return SYNC;
        }

        for (RequestType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }

        return SYNC;
    }
}

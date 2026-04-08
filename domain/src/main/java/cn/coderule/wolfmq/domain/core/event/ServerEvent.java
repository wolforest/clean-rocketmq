
package cn.coderule.wolfmq.domain.core.event;

import lombok.Getter;

@Getter
public enum ServerEvent {
    SERVER_START,

    BECOME_MASTER,
    BECOME_SLAVE,
    BECOME_BACKUP,

    SERVER_STOP;


}

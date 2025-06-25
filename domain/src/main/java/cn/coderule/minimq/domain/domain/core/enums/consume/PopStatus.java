package cn.coderule.minimq.domain.domain.core.enums.consume;

public enum PopStatus {
    /**
     * Founded
     */
    FOUND,
    /**
     * No new message can be pull after polling time out
     * delete after next realease
     */
    NO_NEW_MSG,
    /**
     * polling pool is full, do not try again immediately.
     */
    POLLING_FULL,
    /**
     * polling time out but no message find
     */
    POLLING_NOT_FOUND
}

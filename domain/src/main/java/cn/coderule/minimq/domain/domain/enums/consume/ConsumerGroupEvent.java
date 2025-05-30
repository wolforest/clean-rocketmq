package cn.coderule.minimq.domain.domain.enums.consume;

public enum ConsumerGroupEvent {

    /**
     * Some consumers in the group are changed.
     */
    CHANGE,
    /**
     * The group of consumer is unregistered.
     */
    UNREGISTER,
    /**
     * The group of consumer is registered.
     */
    REGISTER,
    /**
     * The client of this consumer is new registered.
     */
    CLIENT_REGISTER,
    /**
     * The client of this consumer is unregistered.
     */
    CLIENT_UNREGISTER
}

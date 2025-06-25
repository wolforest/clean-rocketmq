

/**
 * $Id: ConsumeType.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.domain.core.enums.consume;

public enum ConsumeType {

    CONSUME_ACTIVELY("PULL"),

    CONSUME_PASSIVELY("PUSH"),

    CONSUME_POP("POP");

    private final String typeCN;

    ConsumeType(String typeCN) {
        this.typeCN = typeCN;
    }

    public String getTypeCN() {
        return typeCN;
    }
}

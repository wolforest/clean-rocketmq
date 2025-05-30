package cn.coderule.minimq.rpc.common.rpc.core.enums;

public enum BoundaryType {
    /**
     * Indicate that lower boundary is expected.
     */
    LOWER("lower"),

    /**
     * Indicate that upper boundary is expected.
     */
    UPPER("upper");

    private final String name;

    BoundaryType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static BoundaryType getType(String name) {
        if (BoundaryType.UPPER.getName().equalsIgnoreCase(name)) {
            return UPPER;
        }
        return LOWER;
    }
}

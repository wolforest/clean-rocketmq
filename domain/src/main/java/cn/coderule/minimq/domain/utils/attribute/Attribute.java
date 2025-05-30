package cn.coderule.minimq.domain.utils.attribute;

public abstract class Attribute {
    protected String name;
    protected boolean changeable;

    public abstract void verify(String value);

    public Attribute(String name, boolean changeable) {
        this.name = name;
        this.changeable = changeable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChangeable() {
        return changeable;
    }

    public void setChangeable(boolean changeable) {
        this.changeable = changeable;
    }
}

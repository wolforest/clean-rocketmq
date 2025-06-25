package cn.coderule.minimq.domain.domain.meta.subscription;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 *
 */
public class GroupForbidden implements Serializable {

    private String  topic;
    private String  group;
    private Boolean readable;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Boolean getReadable() {
        return readable;
    }

    public void setReadable(Boolean readable) {
        this.readable = readable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((readable == null) ? 0 : readable.hashCode());
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupForbidden other = (GroupForbidden) obj;
        return new EqualsBuilder()
                .append(topic, other.topic)
                .append(group, other.group)
                .append(readable, other.readable)
                .isEquals();
    }

    @Override
    public String toString() {
        return "GroupForbidden [topic=" + topic + ", group=" + group + ", readable=" + readable + "]";
    }

}

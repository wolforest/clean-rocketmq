package cn.coderule.minimq.domain.domain.meta.topic;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResult implements Serializable {

    private Topic topic;

}

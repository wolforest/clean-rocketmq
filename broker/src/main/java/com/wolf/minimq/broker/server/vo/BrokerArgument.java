package com.wolf.minimq.broker.server.vo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerArgument implements Serializable {
    private String rootPath;
    private String nameServ;

    public void validate() {

    }
}

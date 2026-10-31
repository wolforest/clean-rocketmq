package com.wolfmq.domain.monitor.model;

import java.io.Serializable;
import lombok.Data;
import org.springframework.jmx.support.MetricType;

@Data
public class Metric implements Serializable {
    private String name;
    private long value = 0;
    private String description = "";

    private String group;
    /**
     * metrics unit:
     *  - time: ns/ms/s/m/h/d/...
     *  - count/byte/
     */
    private String unit = "";

    private MetricType type = MetricType.COUNTER;
    /**
     * metrics scale:
     * - decimals: if scale < 0
     * - integer:  if scale = 0
     * - ceiling:  if scale > 0
     */
    private int scale = 0;

}

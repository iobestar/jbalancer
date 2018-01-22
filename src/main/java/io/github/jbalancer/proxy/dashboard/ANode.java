package io.github.jbalancer.proxy.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ANode {

    private String balancer;
    private Map<String,String> labels;
    private String connection;
    private String status;

    private boolean alive;
    private boolean active;
    private boolean enabled;

    private String checkStatus;
}

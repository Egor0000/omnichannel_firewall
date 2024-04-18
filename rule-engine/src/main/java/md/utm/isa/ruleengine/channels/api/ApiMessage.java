package md.utm.isa.ruleengine.channels.api;

import lombok.Data;

import java.util.HashMap;

@Data
public class ApiMessage {
    private String messageId;
    private HashMap<String, String> headers = new HashMap<>();

    private String from;
    private String customer;
    private String sessionId;
    private String agentId;
    private String body;

    private String method;

    private HashMap<String, String> customParams = new HashMap<>();

}

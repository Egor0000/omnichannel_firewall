package md.utm.isa.ruleengine.engine;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FilterResponse {
    String messageId;
    MessageType messageType;
    Map<String, String> actions = new HashMap<>();
    String matchedStatement;
    String matchesRegex;
}

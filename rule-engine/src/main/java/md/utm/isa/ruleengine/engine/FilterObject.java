package md.utm.isa.ruleengine.engine;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FilterObject {
    private MessageType messageType;
    private String messageId;
    private String from;
    private String to;
    private String messageBody;
    private Map<String, Object> customParams = new HashMap<>();

    public Object getValueByFieldName(String fieldName) {
        return switch (fieldName) {
            case "messageType" -> messageType;
            case "messageId" -> messageId;
            case "from" -> from;
            case "to" -> to;
            case "messageBody" -> messageBody;
            default -> customParams != null ? customParams.get(fieldName) : null;
        };
    }
}

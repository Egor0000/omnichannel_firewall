package md.utm.isa.apiingestor.filtering;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FilterResponse {
    String messageId;
    String messageType;
    Map<String, String> actions = new HashMap<>();
    String matchedStatement;
    String matchesRegex;
}

package md.utm.isa.ruleengine.engine;

import lombok.Data;

@Data
public class FilterObject {
    private String messageId;
    private String from;
    private String to;
    private String messageBody;
}

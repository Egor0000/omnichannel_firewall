package md.utm.isa.ruleengine.engine;

import lombok.Data;

@Data
public class FilterResponse {
    MessageType messageType;
    FilterResponseAction filterResponseAction;
}

package md.utm.isa.ruleengine.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Action {
    private String tag;
    private String value;
}

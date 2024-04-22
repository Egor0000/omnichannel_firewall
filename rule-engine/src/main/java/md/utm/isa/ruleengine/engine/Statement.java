package md.utm.isa.ruleengine.engine;

import com.bpodgursky.jbool_expressions.Expression;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.utm.isa.ruleengine.config.JacksonXmlAnnotation;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statement {
    private String name;
    @JacksonXmlElementWrapper(localName = "actions")
    @JacksonXmlProperty(localName = "action")
    private List<Action> actions;

    // todo try to parse statement to expression
    @JacksonXmlAnnotation.JsonOnly
    private Expression<String> expression;

    public Statement(String name, List<Action> actions) {
        this.name = name;
        this.actions = actions;
    }
}

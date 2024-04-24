package md.utm.isa.ruleengine.engine;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.config.JacksonXmlAnnotation;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Statement {
    private String value;
    @JacksonXmlElementWrapper(localName = "actions")
    @JacksonXmlProperty(localName = "action")
    private List<Action> actions;

    // todo try to parse statement to expression
    @JacksonXmlAnnotation.JsonOnly
    private Expression<String> expression;



    public void setValue(String value) {
        // Your processing logic here
        try {
            expression = ExprParser.parse(value);
        } catch (Exception e) {
            log.error("Skipped mapping value {} to expression in statement. Error {}", value, e.getMessage());
        }
        this.value = value;
    }

    public Statement(String value, List<Action> actions) {
        this.value = value;
        this.actions = actions;
        log.info("Statement {}", value);

    }
}

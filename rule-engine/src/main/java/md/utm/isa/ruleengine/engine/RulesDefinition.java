package md.utm.isa.ruleengine.engine;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
public class RulesDefinition {
    @JacksonXmlElementWrapper(localName = "rules")
    @JacksonXmlProperty(localName = "rule")
    private List<Rule> rules;
    @JacksonXmlElementWrapper(localName = "statements")
    @JacksonXmlProperty(localName = "statement")
    private List<Statement> statements;
}

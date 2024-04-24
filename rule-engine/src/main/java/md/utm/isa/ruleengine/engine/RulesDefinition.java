package md.utm.isa.ruleengine.engine;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import md.utm.isa.ruleengine.config.JacksonXmlAnnotation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Data
public class RulesDefinition {
    @JacksonXmlElementWrapper(localName = "rules")
    @JacksonXmlProperty(localName = "rule")
    private List<Rule> rules;
    @JacksonXmlElementWrapper(localName = "statements")
    @JacksonXmlProperty(localName = "statement")
    private List<Statement> statements;
    @JacksonXmlAnnotation.JsonOnly
    private Map<String, Rule> rulesMap = new ConcurrentHashMap<>();

    public void setRules(List<Rule> rules) {
        this.rules = rules;
        rulesMap = rules.stream().collect(Collectors.toMap(Rule::getName, obj -> obj));
    }
}

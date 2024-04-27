package md.utm.isa.ruleengine.engine;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sun.source.doctree.SeeTree;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.config.RuleEngineProperties;
import md.utm.isa.ruleengine.config.XmlAnnotationIntrospector;
import md.utm.isa.ruleengine.exceptions.RuleEvaluationException;
import md.utm.isa.ruleengine.exceptions.StatementParsingException;
import md.utm.isa.ruleengine.util.FileUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {
    private final XmlMapper xmlMapper = new XmlMapper();
    private ObjectWriter objectWriter;
    private final RuleEngineProperties ruleEngineProperties;
    private RulesDefinition rulesDefinition;
    private Map<String, Pattern> regexPattern = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        xmlMapper.setAnnotationIntrospector(new XmlAnnotationIntrospector());
        objectWriter = xmlMapper.writerWithDefaultPrettyPrinter();
        parse();
    }

    public FilterResponse filterMessage(FilterObject filterObject) {
        FilterResponse filterResponse = new FilterResponse();
        filterResponse.setMessageId(filterObject.getMessageId());
        for (Statement statement: rulesDefinition.getStatements()) {
            Expression<String> evalExpression = statement.getExpression();
            HashMap<String, Boolean> matches = new HashMap<>();
            for (String k: evalExpression.getAllK()) {
                Rule rule = rulesDefinition.getRulesMap().get(k);
                boolean ruleResult = evalRule(rule, filterObject);
                log.info("Rule result {} for rule {}", ruleResult, rule);
                matches.put(k, ruleResult);
            }
            Expression<String> resolved =  RuleSet.assign(evalExpression, matches);
            try {
                // could use resolved.getExprType() == "literal" to check if it is a true/false result
                Boolean parsedResult = Boolean.parseBoolean(resolved.toString());
                log.info("Resolved rule {}", parsedResult);

                if (parsedResult.equals(Boolean.TRUE)) {
                    Map<String, String> actionsMap = statement.getActions().stream().collect(Collectors.toMap(Action::getTag, Action::getValue));
                    filterResponse.setActions(actionsMap);
                    filterResponse.setMatchedStatement(statement.getValue().toString());
                    return filterResponse;
                }

            } catch (Exception ex) {
                log.error("Could get parsedResult {}. Most probably the expression is not assigned with all required boolean values", resolved);
            }
        }

        //todo add custom fields to filter request and think about external measurements like size, word count (may be add some function capabilities)

        //todo for testing: add filters for suspicious email address
        //todo for spam content: Urgent tone,Generic greetings, URLs, websites

        // if any of filters are matched, ALLOW by default
        filterResponse.getActions().put("action", "ALLOW");

        return filterResponse;
    }

    private boolean evalRule(Rule rule, FilterObject filterObject) {
        log.info("Evaluating rule {}", rule.getName());

        Object left = rule.getLeft();
        Object right = rule.getRight();

        if (left == null || right == null) {
            throw new RuleEvaluationException("Rule " + rule.getName() + " has no left and/or right expression");
        }

        left = processRuleParts(rule.getLeft(), filterObject);
        right = processRuleParts(rule.getRight(), filterObject);

        Operation operation = rule.getOperation();

        switch (operation) {
            case IN -> {
                return isMatch(String.format(".*%s.*", left.toString()), right.toString());
            }
            case CONTAINS -> {
                return isMatch(String.format(".*%s.*", right.toString()), left.toString());
            }
            case EQUAL -> {
                return right.equals(left);
            }
            case NOT_IN -> {
                return !isMatch(String.format(".*%s.*", left.toString()), right.toString());
            }
            case NOT_CONTAINS -> {
                return !isMatch(String.format(".*%s.*", right.toString()), left.toString());
            }
            case NOT_EQUAL -> {
                return !right.equals(left);
            }
            case LESS -> {
                try {
                    double leftDouble = Double.parseDouble(left.toString());
                    double rightDouble = Double.parseDouble(right.toString());
                    return leftDouble < rightDouble;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            case GREATER -> {
                try {
                    double leftDouble = Double.parseDouble(left.toString());
                    double rightDouble = Double.parseDouble(right.toString());
                    return leftDouble > rightDouble;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            case NOT_GREATER -> {
                try {
                    double leftDouble = Double.parseDouble(left.toString());
                    double rightDouble = Double.parseDouble(right.toString());
                    return leftDouble <= rightDouble;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            case NOT_LESS -> {
                try {
                    double leftDouble = Double.parseDouble(left.toString());
                    double rightDouble = Double.parseDouble(right.toString());
                    return leftDouble >= rightDouble;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }



        return false;
    }

    private Object processRuleParts(Object part, FilterObject filterObject) {

        String regex = "\\{\\{([a-zA-Z_$][a-zA-Z\\d_$]*)}}";
        if (part instanceof String stringPart && isMatch(regex, stringPart)) {
            Object value =  filterObject.getValueByFieldName(stringPart.replaceAll("^\\{\\{(.*)}}$", "$1"));
            return value != null ? value : "";
        }
        return part;
    }

    private void checkExpressionContainsValidRules(Expression<String> evalExpression, List<Rule> rules) {
        Set<String> rulesNames = rules.stream().map(Rule::getName).collect(Collectors.toSet());
        Set<String> statementOperands = evalExpression.getAllK();
        for (String k: statementOperands) {
            if (!rulesNames.contains(k)) {
                throw new StatementParsingException("No valid rule found for expression: " +  evalExpression + ". Statement operand " + k + " not found in rule set");
            }
        }
    }

    public void serializeAndStore(RulesDefinition rulesDefinition) throws IOException {
        objectWriter.writeValue(FileUtil.getFile(ruleEngineProperties.getConfigFile()), rulesDefinition);
    }

    public RulesDefinition parse() throws IOException {
        File file = FileUtil.getFile(ruleEngineProperties.getConfigFile());
        rulesDefinition = xmlMapper.readValue(file, RulesDefinition.class);

        //todo add validation:
        // 1. for ruleOp
        for (Statement statement: rulesDefinition.getStatements()) {
            Expression<String> evalExpression = statement.getExpression();
            checkExpressionContainsValidRules(evalExpression, rulesDefinition.getRules());
        }

        return rulesDefinition;
    }

    private boolean isMatch(String pattern, String text) {
        Pattern compiledPattern = regexPattern.computeIfAbsent(pattern, Pattern::compile);
        Matcher matcher = compiledPattern.matcher(text);
        return matcher.matches();
    }

}

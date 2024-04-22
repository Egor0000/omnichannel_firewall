package md.utm.isa.ruleengine.statement;


import com.bpodgursky.jbool_expressions.*;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class StatementTest {

    @Test
     public void testExpression() {
         Expression<String> expr = And.of(
                 Variable.of("A"),
                 Variable.of("B"),
                 Or.of(Variable.of("C"), Not.of(Variable.of("C"))));

         HashMap<String, Boolean> booleanBooleanHashMap = new HashMap<>();
         booleanBooleanHashMap.put("A", true);
         booleanBooleanHashMap.put("B", true);
         booleanBooleanHashMap.put("C", true);

         Expression<String> halfAssigned = RuleSet.assign(expr, booleanBooleanHashMap);

         Assertions.assertTrue(Boolean.parseBoolean(halfAssigned.toString()));
     }

    @Test
    public void testParsing() {
        Expression<String> parsedExpression = RuleSet.simplify(ExprParser.parse("(((!C)|C)&A&B)"));

        HashMap<String, Boolean> booleanBooleanHashMap = new HashMap<>();
        booleanBooleanHashMap.put("A", true);
        booleanBooleanHashMap.put("B", true);
        booleanBooleanHashMap.put("C", true);


        Assertions.assertNotNull(parsedExpression);

        Expression<String> result = RuleSet.assign(parsedExpression, booleanBooleanHashMap);

        Assertions.assertTrue(Boolean.parseBoolean(result.toString()));
    }
}

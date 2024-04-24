package md.utm.isa.ruleengine.ruleengine;

import md.utm.isa.ruleengine.engine.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.tools.FileObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(properties = { "engine.firstProperty=src/main/resources/rule_engine_config.xml" })
public class RuleEngineTest {
    @Autowired
    private RuleEngine ruleEngine;


    @Test
    void testSerializingRuleDefinition() throws IOException {
        RulesDefinition rulesDefinition = new RulesDefinition();
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("t1", "String", "Left", Operation.CONTAINS));
        rules.add(new Rule("t2", "Regex", "Left", Operation.IN));
        rules.add(new Rule("t3", "KEk", "Right", Operation.NOT_GREATER));
        rulesDefinition.setRules(rules);
        List<Statement> statements = new ArrayList<>();
        statements.add(new Statement("(t1|t2)&(!(t3)|t2)", List.of(new Action("action", "BLOCK"), new Action("code", "33"))));
        rulesDefinition.setStatements(statements);

        String expected = """
                <RulesDefinition>
                  <rules>
                    <rule>
                      <name>t1</name>
                      <left>String</left>
                      <right>Left</right>
                      <operation>CONTAINS</operation>
                    </rule>
                    <rule>
                      <name>t2</name>
                      <left>Regex</left>
                      <right>Left</right>
                      <operation>IN</operation>
                    </rule>
                    <rule>
                      <name>t3</name>
                      <left>KEk</left>
                      <right>Right</right>
                      <operation>NOT_GREATER</operation>
                    </rule>
                  </rules>
                  <statements>
                    <statement>
                      <name>(t1|t2)&amp;(!(t3)|t2)</name>
                      <actions>
                        <action>
                          <tag>action</tag>
                          <value>BLOCK</value>
                        </action>
                        <action>
                          <tag>code</tag>
                          <value>33</value>
                        </action>
                      </actions>
                    </statement>
                  </statements>
                </RulesDefinition>
                """;

        ruleEngine.serializeAndStore(rulesDefinition);

        String realContent = Files.readString(Paths.get("src/main/resources/rule_engine_config.xml"));

        Assertions.assertEquals(expected, realContent);
    }

    @Test
    void testParsingRuleDefinitionFile() throws IOException {
        RulesDefinition rulesDefinition = new RulesDefinition();
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("t1", "String", "Left", Operation.CONTAINS));
        rules.add(new Rule("t2", "Regex", "Left", Operation.IN));
        rules.add(new Rule("t3", "KEk", "Right", Operation.NOT_GREATER));
        rulesDefinition.setRules(rules);
        List<Statement> statements = new ArrayList<>();
        statements.add(new Statement("(t1|t2)&(!(t3)|t2)", List.of(new Action("action", "BLOCK"), new Action("code", "33"))));
        rulesDefinition.setStatements(statements);

        ruleEngine.serializeAndStore(rulesDefinition);

        RulesDefinition actualDefinition = ruleEngine.parse();
        Assertions.assertNotNull(actualDefinition);

        Assertions.assertEquals(rulesDefinition, actualDefinition);
    }

    @Test
    void testRulePatternMatching() {
        FilterObject filterObject = new FilterObject();
        filterObject.setMessageId("123456ID");
        FilterResponse filterResponse =  ruleEngine.filterMessage(filterObject);
    }
}

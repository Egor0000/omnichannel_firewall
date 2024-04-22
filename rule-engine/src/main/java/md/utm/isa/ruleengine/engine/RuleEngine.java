package md.utm.isa.ruleengine.engine;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import md.utm.isa.ruleengine.config.RuleEngineProperties;
import md.utm.isa.ruleengine.config.XmlAnnotationIntrospector;
import md.utm.isa.ruleengine.util.FileUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RuleEngine {
    private final XmlMapper xmlMapper = new XmlMapper();
    private ObjectWriter objectWriter;
    private final RuleEngineProperties ruleEngineProperties;
    private RulesDefinition rulesDefinition;

    @PostConstruct
    public void init() throws IOException {
        xmlMapper.setAnnotationIntrospector(new XmlAnnotationIntrospector());
        objectWriter = xmlMapper.writerWithDefaultPrettyPrinter();
    }

    public FilterResponse filterMessage(FilterObject filterObject) {
        FilterResponse filterResponse = new FilterResponse();
        filterResponse.setFilterResponseAction(FilterResponseAction.ALLOW);

        // todo add logic for evaluating incoming message based on rulesDefinition

        return filterResponse;
    }

    public void serializeAndStore(RulesDefinition rulesDefinition) throws IOException {
        objectWriter.writeValue(FileUtil.getFile(ruleEngineProperties.getConfigFile()), rulesDefinition);
    }

    public RulesDefinition parse() throws IOException {
        File file = FileUtil.getFile(ruleEngineProperties.getConfigFile());
        rulesDefinition = xmlMapper.readValue(file, RulesDefinition.class);

        return rulesDefinition;
    }
}

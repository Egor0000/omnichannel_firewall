package md.utm.isa.ruleengine.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "engine")
@Data
public class RuleEngineProperties {
    private String configFile;
    private String patternFile;
}

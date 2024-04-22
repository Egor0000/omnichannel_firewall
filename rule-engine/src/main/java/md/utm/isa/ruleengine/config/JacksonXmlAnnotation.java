package md.utm.isa.ruleengine.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class JacksonXmlAnnotation {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface JsonOnly {
    }
}


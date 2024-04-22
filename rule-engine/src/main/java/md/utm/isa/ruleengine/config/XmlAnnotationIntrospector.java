package md.utm.isa.ruleengine.config;


import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;

public class XmlAnnotationIntrospector extends JacksonXmlAnnotationIntrospector {
    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        return m.hasAnnotation(JacksonXmlAnnotation.JsonOnly.class) || super.hasIgnoreMarker(m);
    }
}
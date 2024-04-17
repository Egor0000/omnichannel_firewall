package md.utm.isa.ruleengine.engine;

import org.springframework.stereotype.Service;

@Service
public class RuleEngine {
    public FilterResponse filterMessage(FilterObject filterObject) {
        FilterResponse filterResponse = new FilterResponse();
        filterResponse.setFilterResponseAction(FilterResponseAction.ALLOW);
        return filterResponse;
    }
}

package md.utm.isa.ruleengine.channels.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.broker.ApiResponseProducer;
import md.utm.isa.ruleengine.engine.FilterObject;
import md.utm.isa.ruleengine.engine.FilterResponse;
import md.utm.isa.ruleengine.engine.RuleEngine;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiMessageProcessor {
    private final RuleEngine ruleEngine;
    private final Map<String, ApiMessageWrapper> apiMessageWrappers = new HashMap<>();
    private final ApiResponseProducer apiResponseProducer;

    public void processApiMessage(List<ApiMessageWrapper> apiMessageWrappersList) {
        // store mail wrapper to send it back later to proper path
        for (ApiMessageWrapper apiMessageWrapper : apiMessageWrappersList) {
            try {
                FilterObject filterObject = apiWrapperToFilteredObject(apiMessageWrapper);
                FilterResponse filterResponse = ruleEngine.filterMessage(filterObject);
                log.info("Message {} filtered with response {}", filterObject.getMessageId(), filterResponse);
                apiResponseProducer.sendApiResponse(filterResponse);
            } catch (Exception ex) {
                apiMessageWrappers.remove(apiMessageWrapper.getUuid().toString());
                log.error(ex.getMessage(), ex);
            }

        }
    }

    private FilterObject apiWrapperToFilteredObject(ApiMessageWrapper apiMessageWrapper) throws Exception {
        FilterObject filterObject = new FilterObject();
        ApiMessage apiMessage = apiMessageWrapper.getApiMessage();
        if (apiMessage == null) {
            throw new Exception(String.format("Failed to process aoiMessageWrapper from %s. Api message is null", apiMessageWrapper.getUuid().toString()));
        }
        filterObject.setMessageId(apiMessage.getMessageId());
        filterObject.setTo(apiMessage.getCustomer());
        filterObject.setFrom(apiMessage.getFrom());
        filterObject.setMessageBody(apiMessage.getBody());
        return filterObject;
    }

}

package md.utm.isa.apiingestor.api;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ApiStorage {
    private final Map<String, ApiMessage> apis = new ConcurrentHashMap<>();

    public ApiMessage getApiMessage(String messageId) {
        return apis.get(messageId);
    }
    public void storeApiMessage(ApiMessage apiMessage) {
        apis.put(apiMessage.getMessageId(), apiMessage);
    }
}

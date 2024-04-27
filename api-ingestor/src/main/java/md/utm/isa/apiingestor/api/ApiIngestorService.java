package md.utm.isa.apiingestor.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.apiingestor.broker.ApiProducer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiIngestorService {
    private final ApiProducer apiProducer;
    private final ApiStorage apiStorage;

    public void processMessage(ApiMessage message) {
        log.info("Message received: {}", message);

        message.setMessageId(UUID.randomUUID().toString());
        message.setReceivedTimestamp(System.currentTimeMillis());
        apiStorage.storeApiMessage(message);
        apiProducer.sendApiMessage(message);
    }

}


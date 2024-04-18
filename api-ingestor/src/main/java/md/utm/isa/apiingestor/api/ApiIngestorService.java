package md.utm.isa.apiingestor.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.apiingestor.broker.ApiProducer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiIngestorService {
    private final ApiProducer apiProducer;

    public void processMessage(ApiMessage message) {
        log.info("Message received: {}", message);

        apiProducer.sendApiMessage(message);
    }

}


package md.utm.isa.apiingestor.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import md.utm.isa.apiingestor.api.ApiMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ApiProducer {
    private static final Logger log = LoggerFactory.getLogger(ApiProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private final OpenAPI api;
    private UUID consumerId;
    private final Queue<ApiMessage> apiQueue = new ConcurrentLinkedQueue<>();
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        consumerId = UUID.randomUUID();
        processApiMessageQueue();
    }

    public void sendApiMessage(ApiMessage apiMessage) {
        apiQueue.add(apiMessage);
    }

    private void processApiMessageQueue() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            List<ApiMessage> apiMessages = new ArrayList<>();
            while (!apiQueue.isEmpty()) {
                ApiMessage sms = apiQueue.poll();
                apiMessages.add(sms);

                // todo move batch size to property config
                if (apiMessages.size() >= 1000) {
                    sendApiMessageList(apiMessages);
                    apiMessages.clear();
                }
            }

            if (!apiMessages.isEmpty()) {
                sendApiMessageList(apiMessages);
                apiMessages.clear();
            }
            // todo move batch interval to property config
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void sendApiMessageList(List<ApiMessage> apiMessages) {
        try {
            List<ApiWrapper> apiWrappers = apiMessages.stream().map(apiMessage -> new ApiWrapper(consumerId, apiMessage)).toList();
            byte [] bytes = objectMapper.writeValueAsBytes(apiWrappers);
//            String bytes = new ObjectMapper().writeValueAsString(new String("eeeeee"));
            rabbitTemplate.convertAndSend("message.exchange","api-queue.request." + consumerId, bytes);
            log.info("Sent {} api messages", apiWrappers.size());
        } catch (Exception e) {
            log.error("Failed to send api messages to RabbitMq", e);
        }
    }
}

package md.utm.isa.ruleengine.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.engine.FilterResponse;
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
@Slf4j
public class ApiResponseProducer {
    private final RabbitTemplate rabbitTemplate;
    private UUID consumerId;
    private final Queue<FilterResponse> apiResponseQueue = new ConcurrentLinkedQueue<>();
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        consumerId = UUID.randomUUID();
        processMailFilterResponseQueue();
    }

    public void sendApiResponse(FilterResponse filterResponse) {
        apiResponseQueue.add(filterResponse);
    }

    private void processMailFilterResponseQueue() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            List<FilterResponse> apiResponseSendList = new ArrayList<>();
            while (!apiResponseQueue.isEmpty()) {
                FilterResponse filterResponse = apiResponseQueue.poll();
                apiResponseSendList.add(filterResponse);

                if (apiResponseSendList.size() >= 1000) {
                    sendApiResponseList(apiResponseSendList);
                    apiResponseSendList.clear();
                }
            }

            if (!apiResponseSendList.isEmpty()) {
                sendApiResponseList(apiResponseSendList);
                apiResponseSendList.clear();
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void sendApiResponseList(List<FilterResponse> apiResponseSendList) {
        try {
            byte [] bytes = objectMapper.writeValueAsBytes(apiResponseSendList);
            rabbitTemplate.convertAndSend("message.exchange","api-queue.response." + consumerId, bytes);
            log.info("Sent {} mails", apiResponseSendList.size());
        } catch (Exception e) {
            log.error("Failed to send api responses to RabbitMq", e);
        }
    }
}

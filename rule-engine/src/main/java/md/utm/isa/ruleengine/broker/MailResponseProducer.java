package md.utm.isa.ruleengine.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.engine.FilterResponse;
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
@Slf4j
public class MailResponseProducer {
    private final RabbitTemplate rabbitTemplate;
    private UUID consumerId;
    private final Queue<FilterResponse> mailQueue = new ConcurrentLinkedQueue<>();
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        consumerId = UUID.randomUUID();
        processMailFilterResponseQueue();
    }

    public void sendMailResponse(FilterResponse filterResponse) {
        mailQueue.add(filterResponse);
    }

    private void processMailFilterResponseQueue() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            List<FilterResponse> mailFilterResponses = new ArrayList<>();
            while (!mailQueue.isEmpty()) {
                FilterResponse filterResponse = mailQueue.poll();
                mailFilterResponses.add(filterResponse);

                if (mailFilterResponses.size() >= 1000) {
                    sendMailFilterResponse(mailFilterResponses);
                    mailFilterResponses.clear();
                }
            }

            if (!mailFilterResponses.isEmpty()) {
                sendMailFilterResponse(mailFilterResponses);
                mailFilterResponses.clear();
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void sendMailFilterResponse(List<FilterResponse> mailFilterResponses) {
        try {
            byte [] bytes = objectMapper.writeValueAsBytes(mailFilterResponses);
            rabbitTemplate.convertAndSend("message.exchange","mail-queue.response." + consumerId, bytes);
            log.info("Sent {} mails", mailFilterResponses.size());
        } catch (Exception e) {
            log.error("Failed to send mails to RabbitMq", e);
        }
    }
}

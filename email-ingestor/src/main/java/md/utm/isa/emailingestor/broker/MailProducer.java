package md.utm.isa.emailingestor.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import md.utm.isa.emailingestor.server.Mail;
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
public class MailProducer {
    private static final Logger log = LoggerFactory.getLogger(MailProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private UUID consumerId;
    private final Queue<Mail> mailQueue = new ConcurrentLinkedQueue<>();
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        consumerId = UUID.randomUUID();
        processMailQueue();
    }

    public void sendMail(Mail mail) {
        mailQueue.add(mail);
    }

    private void processMailQueue() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            List<Mail> mails = new ArrayList<>();
            while (!mailQueue.isEmpty()) {
                Mail sms = mailQueue.poll();
                mails.add(sms);

                // todo move batch size to property config
                if (mails.size() >= 1000) {
                    sendMailList(mails);
                    mails.clear();
                }
            }

            if (!mails.isEmpty()) {
                sendMailList(mails);
                mails.clear();
            }
            // todo move batch interval to property config
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void sendMailList(List<Mail> mails) {
        try {
            List<MailWrapper> mailWrappers = mails.stream().map(mail -> new MailWrapper(consumerId, mail)).toList();
            byte [] bytes = objectMapper.writeValueAsBytes(mailWrappers);
            rabbitTemplate.convertAndSend("message.exchange","mail-queue.request." + consumerId, bytes);
            log.info("Sent {} mails", mailWrappers.size());
        } catch (Exception e) {
            log.error("Failed to send mails to RabbitMq", e);
        }
    }
}

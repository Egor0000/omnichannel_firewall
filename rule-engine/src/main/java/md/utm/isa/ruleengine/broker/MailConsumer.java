package md.utm.isa.ruleengine.broker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.mail.MailProcessor;
import md.utm.isa.ruleengine.mail.MailWrapper;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailConsumer {
    private final MailProcessor mailProcessor;
    private final ObjectMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mail-queue.request", durable = "true"),
            exchange = @Exchange(value = "message.exchange", type = ExchangeTypes.TOPIC),
            key = "mail-queue.request.#"
    ))
    public void onMailList(byte[] body) {
        try {
            List<MailWrapper> list = objectMapper.readValue(body, new TypeReference<List<MailWrapper>>() {});
            mailProcessor.processMail(list);
        } catch (Exception ex) {
            log.error("Failed to decode incoming mail list", ex);
        }
    }
}

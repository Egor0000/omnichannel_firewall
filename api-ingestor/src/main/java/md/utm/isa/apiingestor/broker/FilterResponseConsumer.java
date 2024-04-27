package md.utm.isa.apiingestor.broker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.emailingestor.filtering.FilterResponse;
import md.utm.isa.emailingestor.server.EmailClient;
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
public class FilterResponseConsumer {
    private final ObjectMapper objectMapper;
    private final EmailClient emailClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mail-filter-responses", durable = "true"),
            exchange = @Exchange(value = "message.exchange", type = ExchangeTypes.TOPIC),
            key = "mail-queue.response.#"
    ))
    public void onFilterResponse(byte[] body) {
        try {
            List<FilterResponse> list = objectMapper.readValue(body, new TypeReference<List<FilterResponse>>() {});
            emailClient.processFilterResponses(list);
        } catch (Exception ex) {
            log.error("Failed to decode incoming api message list", ex);
        }
    }
}

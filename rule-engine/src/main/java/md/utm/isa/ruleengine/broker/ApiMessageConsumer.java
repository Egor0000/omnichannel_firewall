package md.utm.isa.ruleengine.broker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.channels.api.ApiMessageProcessor;
import md.utm.isa.ruleengine.channels.api.ApiMessageWrapper;
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
public class ApiMessageConsumer {
    private final ObjectMapper objectMapper;
    private final ApiMessageProcessor apiMessageProcessor;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "api-message-queue.request", durable = "true"),
            exchange = @Exchange(value = "message.exchange", type = ExchangeTypes.TOPIC),
            key = "api-queue.request.#"
    ))
    public void onAPiMessageList(byte[] body) {
        try {
            List<ApiMessageWrapper> list = objectMapper.readValue(body, new TypeReference<List<ApiMessageWrapper>>() {});
            apiMessageProcessor.processApiMessage(list);
        } catch (Exception ex) {
            log.error("Failed to decode incoming api message list", ex);
        }
    }
}

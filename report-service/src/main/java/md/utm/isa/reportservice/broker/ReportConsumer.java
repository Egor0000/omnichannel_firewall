package md.utm.isa.reportservice.broker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.reportservice.report.Report;
import md.utm.isa.reportservice.report.ReportService;
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
public class ReportConsumer {
    private final ObjectMapper objectMapper;
    private final ReportService reportService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "report-queue", durable = "true"),
            exchange = @Exchange(value = "report.exchange", type = ExchangeTypes.TOPIC),
            key = "report-queue.#"
    ))
    public void onReportService(byte[] body) {
        try {
            List<Report> list = objectMapper.readValue(body, new TypeReference<List<Report>>() {});
            reportService.processReportList(list);
        } catch (Exception ex) {
            log.error("Failed to decode incoming report list", ex);
        }
    }
}

package md.utm.isa.apiingestor.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ReportProducer {
    private final RabbitTemplate rabbitTemplate;
    private UUID consumerId;
    private final Queue<Report> reportQueue = new ConcurrentLinkedQueue<>();
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        consumerId = UUID.randomUUID();
        processReportQueue();
    }
    public void sendReport(Report report) {
        reportQueue.add(report);
    }


    private void processReportQueue() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            List<Report> reports = new ArrayList<>();
            while (!reportQueue.isEmpty()) {
                Report report = reportQueue.poll();
                reports.add(report);

                // todo move batch size to property config
                if (reports.size() >= 1000) {
                    sendReportQueue(reports);
                    reports.clear();
                }
            }

            if (!reports.isEmpty()) {
                sendReportQueue(reports);
                reports.clear();
            }
            // todo move batch interval to property config
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void sendReportQueue(List<Report> reports) {
        try {
            byte [] bytes = objectMapper.writeValueAsBytes(reports);
            rabbitTemplate.convertAndSend("report.exchange","report-queue." + consumerId, bytes);
            log.info("Sent {} reports", reports.size());
        } catch (Exception e) {
            log.error("Failed to send reports to RabbitMq", e);
        }
    }

}

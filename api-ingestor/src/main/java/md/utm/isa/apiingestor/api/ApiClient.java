package md.utm.isa.apiingestor.api;

import com.sun.source.doctree.SeeTree;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.apiingestor.broker.ReportProducer;
import md.utm.isa.apiingestor.filtering.FilterResponse;
import md.utm.isa.apiingestor.filtering.Report;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiClient {
    private final ApiStorage apiStorage;
    private final ReportProducer reportProducer;
    private final Map<String, WebClient> callbackClient = new ConcurrentHashMap<>();

    public void processFilterResponses(List<FilterResponse> filterResponseList) {
        for (FilterResponse filterResponse : filterResponseList) {
            processFilterResponse(filterResponse);
        }
    }

    private void processFilterResponse(FilterResponse filterResponse) {
        ApiMessage apiMessage = apiStorage.getApiMessage(filterResponse.getMessageId());

        if (apiMessage == null) {
            log.error("Failed to process filter response {}. ApiMessage with id {} not found in the storage", filterResponse, filterResponse.getMessageId());
            return;
        }

        Map<String, String> actions = filterResponse.getActions();
        switch (actions.get("action")) {
            case "ALLOW":
                processAllowApiMessage(apiMessage, filterResponse);
                break;
            case "BLOCK":
                processBlockedApiMessage(apiMessage, filterResponse);
                break;
        }
        log.info(filterResponse.toString());
    }

    private void processBlockedApiMessage(ApiMessage apiMessage, FilterResponse filterResponse) {
        log.info("Blocked filter response : {}", filterResponse.toString());
        generateReport(apiMessage, filterResponse);
    }

    private void processAllowApiMessage(ApiMessage apiMessage, FilterResponse filterResponse) {
        if (apiMessage.getCallback() != null) {
            String callback = apiMessage.getCallback();

            // Define the data to be sent in the request body (replace with your object)
            // Create a WebClient instance
            WebClient webClient = WebClient.create(callback);
            callbackClient.computeIfAbsent(callback, WebClient::create);

            // Build the POST request
            webClient.post()
                    .uri(callback)  // Replace with your endpoint path
                    .contentType(MediaType.APPLICATION_JSON)  // Set content type
                    .body(BodyInserters.fromValue(apiMessage))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnError(error -> {
                        log.error("Failed to process allow api message {}", filterResponse.getMessageId(), error);
                    })
                    .subscribe();
        } else {
            log.warn("Callback for api-message {} not provided", apiMessage.getMessageId());
        }
        generateReport(apiMessage, filterResponse);
    }

    private void generateReport(ApiMessage apiMessage, FilterResponse filterResponse) {
        Report report = new Report();
        report.setMessageType("API");
        report.setMessageId(apiMessage.getMessageId());
        report.setReceivedTimestamp(apiMessage.getReceivedTimestamp());
        report.setFrom(apiMessage.getFrom());
        report.setTo(apiMessage.getCustomer());

        Map<String, String> reportData = new HashMap<>();

        reportData.put("sessionId", apiMessage.getSessionId());
        reportData.put("agentId", apiMessage.getAgentId());

        report.setReportData(reportData);
        report.setContent(apiMessage.getBody());

        Map<String, String> filterData = new HashMap<>(filterResponse.getActions());
        filterData.put("matchedStatement", filterResponse.getMatchedStatement());
        report.setFilterData(filterData);

        reportProducer.sendReport(report);
    }
}

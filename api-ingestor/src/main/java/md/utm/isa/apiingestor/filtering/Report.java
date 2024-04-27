package md.utm.isa.apiingestor.filtering;

import lombok.Data;

import java.util.Map;

@Data
public class Report {
    private String messageType;
    private long receivedTimestamp;
    private String messageId;
    private String from;
    private String to;
    private Map<String, String> reportData;

    private String content;

    private Map<String, String> filterData;
}

package md.utm.isa.emailingestor.server;

import lombok.Data;

@Data
public class Mail {
    private String messageId;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String body;
}

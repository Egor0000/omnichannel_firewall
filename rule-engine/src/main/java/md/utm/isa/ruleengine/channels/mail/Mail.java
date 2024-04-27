package md.utm.isa.ruleengine.channels.mail;

import lombok.Data;

@Data
public class Mail {
    private String messageId;
    private long receivedTimestamp;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String encoding;
    private String contentType;
    private String content;
    private String emailBody;
}

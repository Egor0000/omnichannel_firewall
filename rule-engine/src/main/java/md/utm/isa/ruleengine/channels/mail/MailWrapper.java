package md.utm.isa.ruleengine.channels.mail;

import lombok.Data;

import java.util.UUID;

@Data
public class MailWrapper {
    private UUID uuid;
    private Mail mail;
}

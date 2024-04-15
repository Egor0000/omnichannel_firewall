package md.utm.isa.emailingestor.broker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.utm.isa.emailingestor.server.Mail;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailWrapper {
    private UUID uuid;
    private Mail mail;
}

package md.utm.isa.ruleengine.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailWrapper {
    private UUID uuid;
    private Mail mail;
}

package md.utm.isa.emailingestor.server;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MailStorage {
    private final Map<String, Mail> storage = new ConcurrentHashMap<>();

    public Mail getMail(String messageId) {
        return storage.get(messageId);
    }
    public void storeMail(Mail mail) {
        storage.put(mail.getMessageId(), mail);
    }
}

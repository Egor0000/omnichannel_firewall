package md.utm.isa.emailingestor.server;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.emailingestor.broker.MailProducer;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServer {
    private final MailProducer mailProducer;
    private final MailStorage mailStorage;

    @PostConstruct
    public void init() {
        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(new SimpleMessageListener() {
            @Override
            public boolean accept(String s, String s1) {
                return true;
            }

            @Override
            public void deliver(String from, String recipient, InputStream data) {
                String content = IOUtils.toString(data);
                log.info("Message delivered to {} from {}. Text: {}", recipient, from, content);
                Mail mail = new Mail();
                mail.setMessageId(UUID.randomUUID().toString());
                mail.setFrom(from);
                mail.setTo(recipient);
                mail.setSubject(content);
                mail.setBody(content);
                mailStorage.storeMail(mail);
                mailProducer.sendMail(mail);
            }
        }));
        smtpServer.setPort(2525);
        smtpServer.start();
    }
}

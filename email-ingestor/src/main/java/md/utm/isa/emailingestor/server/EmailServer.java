package md.utm.isa.emailingestor.server;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class EmailServer {

    @PostConstruct
    public void init() {
        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(new SimpleMessageListener() {
            @Override
            public boolean accept(String s, String s1) {
                return true;
            }

            @Override
            public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
                String content = IOUtils.toString(data);
                log.info("Message delivered to {} from {}. Text: {}", recipient, from, content);
            }
        }));
        smtpServer.setPort(2525);
        smtpServer.start();
    }
}

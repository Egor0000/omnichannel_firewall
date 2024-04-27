package md.utm.isa.emailingestor.server;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.emailingestor.broker.MailProducer;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.helper.SmarterMessageListener;
import org.subethamail.smtp.helper.SmarterMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServer {
    private final MailProducer mailProducer;
    private final MailStorage mailStorage;

    @PostConstruct
    public void init() {
        new SmarterMessageListenerAdapter(new SmarterMessageListener() {
            @Override
            public Receiver accept(String s, String s1) {
                return null;
            }
        });
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
                mail.setReceivedTimestamp(System.currentTimeMillis());
                mail.setMessageId(UUID.randomUUID().toString());
                mail.setFrom(from);
                mail.setTo(recipient);
                mail.setSubject(content);
                try {
                    MimeMessage message = new MimeMessage(null, new ByteArrayInputStream(content.getBytes()));
                    if (message.getRecipients(Message.RecipientType.BCC) != null) {
                        mail.setBcc(Arrays.stream(message.getRecipients(Message.RecipientType.BCC)).toList().stream().map(Address::toString).collect(Collectors.joining()));
                    }

                    if (message.getRecipients(Message.RecipientType.CC) != null) {
                        mail.setCc(Arrays.stream(message.getRecipients(Message.RecipientType.CC)).toList().stream().map(Address::toString).collect(Collectors.joining()));
                    }

                    mail.setSubject(message.getSubject());
                    mail.setContent((String) message.getContent());
                    mail.setContentType(message.getContentType());
                    mail.setEncoding(message.getEncoding());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                mailStorage.storeMail(mail);
                mailProducer.sendMail(mail);
            }
        }));
        smtpServer.setPort(2525);
        smtpServer.start();
    }
}

package md.utm.isa.mockemailclient.client;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailClient {
    private final MailSender mailSender;

    public void sendMail(Mail mail) {
        sendMail(mail.getTo(), mail.getSubject(), mail.getBody());
    }

    private void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}

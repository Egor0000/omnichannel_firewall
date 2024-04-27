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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mail.getFrom());
        message.setTo(mail.getTo());
        message.setSubject(mail.getSubject());
        message.setText(mail.getBody());
//        message.setBcc(mail.getBcc());
//        message.setCc(mail.getCc());
        mailSender.send(message);
    }
}

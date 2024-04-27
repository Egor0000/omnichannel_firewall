package md.utm.isa.emailingestor.server;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.emailingestor.broker.MailAction;
import md.utm.isa.emailingestor.broker.Report;
import md.utm.isa.emailingestor.broker.ReportProducer;
import md.utm.isa.emailingestor.config.EmailConfig;
import md.utm.isa.emailingestor.filtering.FilterResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailClient {
    private final MailStorage mailStorage;
    private final EmailConfig emailConfig;
    private final ReportProducer reportProducer;

    public void processFilterResponses(List<FilterResponse> filterResponseList) {
        for (FilterResponse filterResponse : filterResponseList) {
            processFilterResponse(filterResponse);
        }
    }

    private void processFilterResponse(FilterResponse filterResponse) {
        MailAction mailAction = getMailAction(filterResponse.getActions());
        String action = mailAction.getAction();
        Mail mail = mailStorage.getMail(filterResponse.getMessageId());
        if (mail == null) {
            log.error("Mail not found in the storage");
            return;
        }

        if (action == null) {
            action = ""; // set a value as a placeholder if it is null
        }

        switch (action) {
            case "BLOCK":
                generateReport(mail, filterResponse);
                break;
            case "ALLOW":
            default:
                sendEmail(mail);
                generateReport(mail, filterResponse);
                break;
        }
    }

    private void generateReport(Mail mail, FilterResponse filterResponse) {
        Report report = new Report();
        report.setMessageType("MAIL");
        report.setMessageId(mail.getMessageId());
        report.setReceivedTimestamp(mail.getReceivedTimestamp());
        report.setFrom(mail.getFrom());
        report.setTo(mail.getTo());

        Map<String, String> reportData = new HashMap<>();
        reportData.put("bcc", mail.getBcc());
        reportData.put("cc", mail.getCc());
        reportData.put("subject", mail.getSubject());

        report.setReportData(reportData);
        report.setContent(mail.getContent());

        Map<String, String> filterData = new HashMap<>(filterResponse.getActions());
        filterData.put("matchedStatement", filterResponse.getMatchedStatement());
        report.setFilterData(filterData);

        reportProducer.sendReport(report);
    }

    private void sendEmail(Mail mail) {
        final String username = emailConfig.getUsername();
        final String password = emailConfig.getPassword();



        // SMTP server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Get the Session object
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);
            // Set the sender and recipient addresses
            message.setFrom(new InternetAddress(mail.getFrom()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getTo()));
            if (mail.getBcc() != null && !mail.getBcc().isEmpty()) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(mail.getBcc()));
            }
            if (mail.getCc() != null && !mail.getCc().isEmpty()) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(mail.getCc()));
            }
            if (mail.getSubject() != null && !mail.getSubject().isEmpty()) {
                message.setSubject(mail.getSubject());
            }
            message.setText(mail.getContent());

            // Send the message
            Transport.send(message);

            log.info("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private MailAction getMailAction(Map<String, String> actions) {
        MailAction mailAction = new MailAction();
        for (String action : actions.keySet()) {
            switch (action) {
                case "action":
                    mailAction.setAction(actions.get(action));
                    break;
                case "code":
                    mailAction.setCode(actions.get(action));
                    break;
            }
        }

        return mailAction;
    }

}

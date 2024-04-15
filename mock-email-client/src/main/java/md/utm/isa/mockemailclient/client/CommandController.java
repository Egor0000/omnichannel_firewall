package md.utm.isa.mockemailclient.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/mail")
@RequiredArgsConstructor
public class CommandController {
    private final MailClient mailClient;

    @PostMapping()
    void sendMail(@RequestBody Mail mail) {
        mailClient.sendMail(mail);
    }
}

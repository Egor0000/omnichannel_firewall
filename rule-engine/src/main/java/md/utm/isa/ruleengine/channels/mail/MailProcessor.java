package md.utm.isa.ruleengine.channels.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.broker.MailResponseProducer;
import md.utm.isa.ruleengine.engine.FilterObject;
import md.utm.isa.ruleengine.engine.FilterResponse;
import md.utm.isa.ruleengine.engine.RuleEngine;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailProcessor {
    private final RuleEngine ruleEngine;
    private final MailResponseProducer mailResponseProducer;
    private final Map<String, MailWrapper> mails = new HashMap<>();

    public void processMail(List<MailWrapper> mailWrappersList) {
        // store mail wrapper to send it back later to proper path
        for (MailWrapper mailWrapper : mailWrappersList) {
            try {
                FilterObject filterObject = mailToFilteredObject(mailWrapper);
                FilterResponse filterResponse = ruleEngine.filterMessage(filterObject);
                mailResponseProducer.sendMailResponse(filterResponse);
                log.info("Message {} filtered with response {}", filterObject.getMessageId(), filterResponse);
            } catch (Exception ex) {
                mails.remove(mailWrapper.getUuid().toString());
                log.error(ex.getMessage(), ex);
            }

        }
    }

    private FilterObject mailToFilteredObject(MailWrapper mailWrapper) throws Exception {
        FilterObject filterObject = new FilterObject();
        Mail mail = mailWrapper.getMail();
        if (mail == null) {
            throw new Exception(String.format("Failed to process mailWrapper from %s. Mail is null", mailWrapper.getUuid().toString()));
        }
        filterObject.setMessageId(mail.getMessageId());
        filterObject.setTo(mail.getTo());
        filterObject.setFrom(mail.getFrom());
        filterObject.setMessageBody(mail.getContent());
        return filterObject;
    }
}

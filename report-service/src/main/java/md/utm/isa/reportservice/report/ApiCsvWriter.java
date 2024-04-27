package md.utm.isa.reportservice.report;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ApiCsvWriter {
    private static final Object lock = new Object();
    private CSVFormat csvFormat;
    private StringWriter stringWriter ;
    private CSVPrinter csvPrinter;

    @PostConstruct
    public void init() throws IOException {
        csvFormat = CSVFormat.DEFAULT.builder().setQuoteMode(QuoteMode.ALL).setAllowMissingColumnNames(true).setTrim(true).build();
        stringWriter = new StringWriter();
        csvPrinter = new CSVPrinter(stringWriter, csvFormat);
    }

    public void writeToCsv(Report mail) {
        List<String> strings = new ArrayList<>();
        strings.add(mail.getMessageType());
        strings.add(mail.getMessageId());
        strings.add(new Date(mail.getReceivedTimestamp()).toString());
        strings.add(mail.getFrom());
        strings.add(mail.getTo());
        strings.add(getFieldData(mail.getReportData().get("subject"), ""));
        strings.add(mail.getContent().replace("\r\n", "\\n"));

        // add filter result
        strings.add(getFieldData(mail.getFilterData().get("action"), ""));
        strings.add(getFieldData(mail.getFilterData().get("code"), "0"));
        strings.add(getFieldData(mail.getFilterData().get("matchedStatement"), ""));

        synchronized (lock) {
            try  {
                csvPrinter.printRecord(strings);
                csvPrinter.flush();
                log.info("{}", stringWriter.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFieldData(String value, String defaultValue) {
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
}

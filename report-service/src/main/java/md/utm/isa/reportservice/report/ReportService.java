package md.utm.isa.reportservice.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    private final MailCsvWriter mailCsvWriter;
    private final ApiCsvWriter apiCsvWriter;

    public void processReportList(List<Report> reportList) {
        for (Report report : reportList) {
            log.info("Processing report {}", report);
            switch (report.getMessageType()) {
                case "MAIL":
                    mailCsvWriter.writeToCsv(report);
                    break;
                case "API":
                    apiCsvWriter.writeToCsv(report);
                    break;
            }
        }
    }
}

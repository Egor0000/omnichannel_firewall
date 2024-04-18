package md.utm.isa.apiingestor.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class Controller {
    private final ApiIngestorService apiIngestorService;

    @PostMapping("/message")
    public void message(@RequestBody ApiMessage apiMessage) {
        apiIngestorService.processMessage(apiMessage);
    }
}

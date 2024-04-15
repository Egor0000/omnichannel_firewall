package md.utm.isa.mockemailclient.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Mail {
    @Schema(example = "your-client@example.com")
    private String from;
    @Schema(example = "your-server@example.com")
    private String to;
    @Schema(example = "second-recipient@example.com")
    private String cc;
    @Schema(example = "third-recipient@example.com")
    private String bcc;
    @Schema(example = "Hello world")
    private String subject;
    @Schema(example = "Something useful")
    private String body;
}

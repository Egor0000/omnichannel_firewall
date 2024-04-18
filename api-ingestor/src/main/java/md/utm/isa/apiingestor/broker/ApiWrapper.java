package md.utm.isa.apiingestor.broker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.utm.isa.apiingestor.api.ApiMessage;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiWrapper {
    private UUID uuid;
    private ApiMessage apiMessage;
}

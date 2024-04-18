package md.utm.isa.ruleengine.channels.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiMessageWrapper {
    private UUID uuid;
    private ApiMessage apiMessage;
}

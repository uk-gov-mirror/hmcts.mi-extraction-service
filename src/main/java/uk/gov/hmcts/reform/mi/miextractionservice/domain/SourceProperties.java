package uk.gov.hmcts.reform.mi.miextractionservice.domain;

import lombok.Builder;
import lombok.Value;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@Builder
@Value
@ConstructorBinding
public class SourceProperties {

    boolean enabled;
    String prefix;
    String dateField;
    String timezone;
    SourceTypeEnum type;
    List<String> filters;
}

package uk.gov.hmcts.reform.mi.miextractionservice.domain;

import lombok.Data;

import java.util.Map;

@Data
public class ExportProperties {

    Map<String, SourceProperties> sources;
}

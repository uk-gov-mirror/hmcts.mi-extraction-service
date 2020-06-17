package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

@Component
public class SourceUtil {

    public String getContainerName(SourceEnum sourceEnum) {
        switch (sourceEnum) {
            case CORE_CASE_DATA:
                return "ccd";
            case NOTIFY:
                return "notify";
            default:
                return "unknown";
        }
    }
}

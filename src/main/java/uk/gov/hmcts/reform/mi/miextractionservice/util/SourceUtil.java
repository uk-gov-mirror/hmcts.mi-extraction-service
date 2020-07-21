package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.PAYMENT_ALLOCATION_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.PAYMENT_FEE_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.PAYMENT_HISTORY_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.PAYMENT_REMISSION_NAME;

@Component
public class SourceUtil {

    public String getContainerName(SourceEnum sourceEnum) {
        switch (sourceEnum) {
            case CORE_CASE_DATA:
                return "ccd";
            case NOTIFY:
                return "notify";
            case PAYMENT_HISTORY:
                return PAYMENT_HISTORY_NAME;
            case PAYMENT_ALLOCATION:
                return PAYMENT_ALLOCATION_NAME;
            case PAYMENT_REMISSION:
                return PAYMENT_REMISSION_NAME;
            case PAYMENT_FEE:
                return PAYMENT_FEE_NAME;
            default:
                return "unknown";
        }
    }
}

package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NO_FILTER_VALUE;

@Component
public class CoreCaseDataFilterComponentImpl implements FilterComponent<CoreCaseData> {

    @Value("${filter.ccd.case-type}")
    private String filterCaseType;

    @Autowired
    private DataParserComponent<CoreCaseData> dataParserComponent;

    @Override
    public List<String> filterDataInDateRange(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {
        return data.stream()
            .filter(ccdString -> {
                CoreCaseData coreCaseData = dataParserComponent.parse(ccdString);

                return filterByDate(coreCaseData, fromDate, toDate) && filterByCaseType(coreCaseData);
            })
            .collect(Collectors.toList());
    }

    private boolean filterByDate(CoreCaseData coreCaseData, OffsetDateTime fromDate, OffsetDateTime toDate) {
        LocalDate eventCreatedDate = coreCaseData.getCeCreatedDate().toLocalDate();

        // Minus and plus 1 day to account to include same day events
        return eventCreatedDate.isAfter(fromDate.toLocalDate().minusDays(1L))
            && eventCreatedDate.isBefore(toDate.toLocalDate().plusDays(1L));
    }

    private boolean filterByCaseType(CoreCaseData coreCaseData) {
        if (filterCaseType.equalsIgnoreCase(NO_FILTER_VALUE)) {
            return true;
        }

        return filterCaseType.equalsIgnoreCase(coreCaseData.getCeCaseTypeId());
    }
}

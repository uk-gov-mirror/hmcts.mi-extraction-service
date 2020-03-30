package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NO_FILTER_VALUE;

@Component
public class CoreCaseDataFilterComponentImpl implements FilterComponent<CoreCaseData> {

    @Value("${filter.case-type}")
    private String filterCaseType;

    @Autowired
    private DataParserComponent<CoreCaseData> dataParserComponent;

    @Override
    public List<CoreCaseData> filterDataInDateRange(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {

        List<CoreCaseData> filteredData = data.stream()
            .map(dataRow -> dataParserComponent.parse(dataRow))
            .filter(coreCaseData -> {
                OffsetDateTime eventCreatedDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(coreCaseData.getCeCreatedDate()), ZoneOffset.UTC);
                return eventCreatedDate.isEqual(fromDate) || eventCreatedDate.isEqual(toDate)
                    || eventCreatedDate.isAfter(fromDate) && eventCreatedDate.isBefore(toDate);
            })
            .collect(Collectors.toList());

        filteredData = filterByCaseType(filteredData);

        return filteredData;
    }

    private List<CoreCaseData> filterByCaseType(List<CoreCaseData> data) {
        return data.stream()
            .filter(coreCaseData -> {
                if (filterCaseType.equalsIgnoreCase(NO_FILTER_VALUE)) {
                    return true;
                } else {
                    return filterCaseType.equalsIgnoreCase(coreCaseData.getCeCaseTypeId());
                }
            }).collect(Collectors.toList());
    }
}

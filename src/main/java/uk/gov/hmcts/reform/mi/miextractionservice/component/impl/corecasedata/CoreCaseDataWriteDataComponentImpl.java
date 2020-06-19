package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.JsonlWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.WriteDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;

import java.io.BufferedWriter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("ccd")
public class CoreCaseDataWriteDataComponentImpl implements WriteDataComponent {

    @Autowired
    private FilterComponent<CoreCaseData> filterComponent;

    @Autowired
    private CoreCaseDataFormatterComponent<OutputCoreCaseData> coreCaseDataFormatterComponent;

    @Autowired
    private JsonlWriterComponent<OutputCoreCaseData> jsonlWriterComponent;

    @Override
    public void writeData(BufferedWriter writer, List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {
        List<CoreCaseData> filteredData = filterComponent.filterDataInDateRange(data, fromDate, toDate);
        List<OutputCoreCaseData> formattedData = filteredData.stream()
            .map(coreCaseDataFormatterComponent::formatData)
            .collect(Collectors.toList());

        jsonlWriterComponent.writeLinesAsJsonl(writer, formattedData);
    }
}

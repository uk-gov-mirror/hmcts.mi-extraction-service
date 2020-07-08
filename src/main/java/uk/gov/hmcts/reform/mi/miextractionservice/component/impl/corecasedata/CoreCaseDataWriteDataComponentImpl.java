package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.LineWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.WriteDataComponent;

import java.io.BufferedWriter;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@Qualifier("ccd")
public class CoreCaseDataWriteDataComponentImpl implements WriteDataComponent {

    @Autowired
    private FilterComponent<CoreCaseData> filterComponent;

    @Autowired
    private LineWriterComponent lineWriterComponent;

    @Override
    public int writeData(BufferedWriter writer, List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {
        List<String> filteredData = filterComponent.filterDataInDateRange(data, fromDate, toDate);

        lineWriterComponent.writeLines(writer, filteredData);

        return filteredData.size();
    }
}

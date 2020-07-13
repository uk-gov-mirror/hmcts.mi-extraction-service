package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.LineWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.WriteDataComponent;

import java.io.BufferedWriter;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@Qualifier("notify")
public class NotifyWriteDataComponentImpl implements WriteDataComponent {

    @Autowired
    @Qualifier("notify")
    private FilterComponent filterComponent;

    @Autowired
    private LineWriterComponent lineWriterComponent;

    @Override
    public int writeData(BufferedWriter writer, List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {
        List<String> filteredData = filterComponent.filterDataInDateRange(data, fromDate, toDate);

        lineWriterComponent.writeLines(writer, filteredData);

        return filteredData.size();
    }
}

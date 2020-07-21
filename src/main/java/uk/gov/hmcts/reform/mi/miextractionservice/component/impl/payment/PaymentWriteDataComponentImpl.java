package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.payment;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.LineWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.WriteDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import java.io.BufferedWriter;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@Component
@Qualifier("payment")
public class PaymentWriteDataComponentImpl implements WriteDataComponent {

    @Qualifier("payment")
    private final FilterComponent filterComponent;

    private final LineWriterComponent lineWriterComponent;

    @Override
    public int writeData(BufferedWriter writer, List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate, SourceEnum source) {
        List<String> filteredData = filterComponent.filterDataInDateRange(data, fromDate, toDate, source);

        lineWriterComponent.writeLines(writer, filteredData);

        return filteredData.size();
    }
}

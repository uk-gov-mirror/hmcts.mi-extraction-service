package uk.gov.hmcts.reform.mi.miextractionservice.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.WriteDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

@Component
public class WriteDataFactory {

    @Autowired
    @Qualifier("ccd")
    private WriteDataComponent ccdWriteDataComponent;

    @Autowired
    @Qualifier("notify")
    private WriteDataComponent notifyWriteDataComponent;

    @SuppressWarnings("PMD.TooFewBranchesForASwitchStatement")
    public WriteDataComponent getWriteComponent(SourceEnum source) {
        switch (source) {
            case CORE_CASE_DATA :
                return ccdWriteDataComponent;
            case NOTIFY :
                return notifyWriteDataComponent;
            default :
                throw new ParserException("Unable to determine how to write data.");
        }
    }
}

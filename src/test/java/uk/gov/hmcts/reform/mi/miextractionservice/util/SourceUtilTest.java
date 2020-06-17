package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class SourceUtilTest {

    private SourceUtil underTest;

    @BeforeEach
    void setUp() {
        underTest = new SourceUtil();
    }

    @Test
    void givenCoreCaseDataSource_whenGetContainerName_thenReturnCcd() {
        assertEquals("ccd", underTest.getContainerName(SourceEnum.CORE_CASE_DATA), "Expected ccd for CoreCaseData source.");
    }

    @Test
    void givenNotifySource_whenGetContainerName_thenReturnNotify() {
        assertEquals("notify", underTest.getContainerName(SourceEnum.NOTIFY), "Expected notify for Notify source.");
    }

    @Test
    void givenUnknownSource_whenGetContainerName_thenReturnUnknown() {
        assertEquals("unknown", underTest.getContainerName(SourceEnum.UNKNOWN), "Expected unknown for Unknown source.");
    }
}

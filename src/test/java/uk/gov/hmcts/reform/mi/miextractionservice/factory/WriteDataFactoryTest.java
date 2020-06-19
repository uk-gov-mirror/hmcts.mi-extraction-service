package uk.gov.hmcts.reform.mi.miextractionservice.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.component.WriteDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class WriteDataFactoryTest {

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Mock
    private WriteDataComponent coreCaseDataWriteDataComponent;

    @Mock
    private WriteDataComponent notifyWriteDataComponent;

    @InjectMocks
    private WriteDataFactory underTest;

    @Test
    void givenCoreCaseDataSource_whenGetWriteDataComponent_thenReturnCoreCaseDataWriteComponent() {
        WriteDataComponent result = underTest.getWriteComponent(SourceEnum.CORE_CASE_DATA);

        assertEquals(coreCaseDataWriteDataComponent, result, "Expected factory to return a core case data writer component.");
    }

    @Test
    void givenNotifySource_whenGetWriteDataComponent_thenReturnNotifyWriteComponent() {
        WriteDataComponent result = underTest.getWriteComponent(SourceEnum.NOTIFY);

        assertEquals(notifyWriteDataComponent, result, "Expected factory to return a notify write component.");
    }

    @Test
    void givenNonExistingSource_whenGetWriteDataComponent_thenThrowException() {
        assertThrows(ParserException.class, () -> underTest.getWriteComponent(SourceEnum.UNKNOWN));
    }
}
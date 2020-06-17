package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
public class NotifyParserComponentImplTest {

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotifyParserComponentImpl underTest;

    @Test
    public void givenNotificationOutputJsonString_whenParseData_thenReturnNotificationOutputObject() {
        NotificationOutput expected = NotificationOutput.builder()
            .extractionDate("19700101-1000")
            .createdAt("2000-01-01")
            .build();

        String jsonInput = "{\"extraction_date\":\"19700101-1000\",\"created_at\":\"2000-01-01\"}";

        assertEquals(expected, underTest.parse(jsonInput), "Result does not match expected properties.");
    }

    @Test
    public void givenNotificationOutputJsonString_whenParseData_thenThrowParserException() {
        assertThrows(ParserException.class, () -> underTest.parse("InvalidJsonString"));
    }
}

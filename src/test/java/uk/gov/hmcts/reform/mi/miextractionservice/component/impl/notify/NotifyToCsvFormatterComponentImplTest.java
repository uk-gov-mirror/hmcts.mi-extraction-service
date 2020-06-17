package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.CsvNotificationOutput;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class NotifyToCsvFormatterComponentImplTest {

    private NotifyToCsvFormatterComponentImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new NotifyToCsvFormatterComponentImpl();
    }

    // PMD not picking up message in assertion for some reason.
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    @Test
    public void givenNotificationOutputData_whenFormatData_thenReturnCsvNotificationOutput() throws Exception {
        String dataString = "{"
            + "\"extraction_date\":\"extractionDate\",\"id\":\"id\",\"service\":\"service\",\"reference\":\"reference\","
            + "\"email_address\":null,\"phone_number\":null,\"line_1\":null,\"line_2\":null,\"line_3\":null,\"line_4\":null,\"line_5\":null,"
            + "\"line_6\":null,\"postcode\":null,"
            + "\"type\":\"type\",\"status\":\"status\","
            + "\"template\":{\"id\":\"templateId\",\"version\":\"templateVersion\",\"uri\":\"templateUri\",\"name\":\"templateName\"},"
            + "\"body\":null,\"subject\":null,"
            + "\"created_at\":\"createdAt\",\"sent_at\":\"sentAt\",\"completed_at\":\"completedAt\""
            + "}";

        NotificationOutput notificationOutput = new ObjectMapper().readValue(dataString, NotificationOutput.class);

        CsvNotificationOutput expected = CsvNotificationOutput
            .builder()
            .extractionDate("extractionDate")
            .id("id")
            .service("service")
            .reference("reference")
            .type("type")
            .status("status")
            .templateId("templateId")
            .templateVersion("templateVersion")
            .templateUri("templateUri")
            .templateName("templateName")
            .createdAt("createdAt")
            .sentAt("sentAt")
            .completedAt("completedAt")
            .build();

        assertEquals(expected, underTest.formatData(notificationOutput), "Properties of output does not match expected values.");
    }
}

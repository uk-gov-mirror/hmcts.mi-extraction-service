package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.model.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.util.ReaderUtil;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_DATA_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_METADATA_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_STATE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_TYPE_VERSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CREATED_DATE_FORMATTED;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_EXTRACTION_DATE;

@ExtendWith(SpringExtension.class)
public class CsvWriterComponentImplTest {

    private static final String TEST_FILE_NAME = "test";

    private String uniqueFileName;

    private CsvWriterComponentImpl underTest;

    @BeforeEach
    public void setUp() {
        uniqueFileName = TEST_FILE_NAME + UUID.randomUUID().toString();

        underTest = new CsvWriterComponentImpl();
    }

    @AfterEach
    public void tearDown() {
        File testFile = new File(uniqueFileName);

        if (testFile.exists()) {
            new File(uniqueFileName).delete();
        }
    }

    @Test
    public void givenValidFilePathAndBeans_whenWriteBeanToCsv_thenCsvFileIsCreated() throws Exception {
        OutputCoreCaseData outputCoreCaseData = new OutputCoreCaseData(
            TEST_EXTRACTION_DATE,
            TEST_CASE_METADATA_EVENT_ID,
            TEST_CASE_DATA_ID,
            TEST_CREATED_DATE_FORMATTED,
            TEST_CASE_TYPE_ID,
            TEST_CASE_TYPE_VERSION,
            TEST_CASE_STATE_ID,
            TEST_DATA_JSON_STRING
        );

        underTest.writeBeansAsCsvFile(uniqueFileName, Collections.singletonList(outputCoreCaseData));

        try (InputStream fileInputStream = Files.newInputStream(Paths.get(uniqueFileName))) {

            List<String> dataAsString = new ReaderUtil().readBytesAsStrings(fileInputStream.readAllBytes());

            String expectedHeaderRow = "\"extraction_date\",\"case_metadata_event_id\",\"ce_case_data_id\","
                + "\"ce_created_date\",\"ce_case_type_id\",\"ce_case_type_version\",\"ce_state_id\",\"data\"";

            // Everything will be wrapped in quotes to prevent data json string commas from affecting actual data
            String expectedDataRow = String.join(",",
                wrapStringInQuotes(TEST_EXTRACTION_DATE),
                wrapStringInQuotes(TEST_CASE_METADATA_EVENT_ID),
                wrapStringInQuotes(TEST_CASE_DATA_ID),
                wrapStringInQuotes(TEST_CREATED_DATE_FORMATTED),
                wrapStringInQuotes(TEST_CASE_TYPE_ID),
                wrapStringInQuotes(TEST_CASE_TYPE_VERSION),
                wrapStringInQuotes(TEST_CASE_STATE_ID),
                wrapStringInQuotes("{\"\"hello\"\":\"\"world\"\"}")
            );

            assertEquals(expectedHeaderRow, dataAsString.get(0), "Header row did not match expected.");
            assertEquals(expectedDataRow, dataAsString.get(1), "Data row did not match expected.");
        }
    }

    private String wrapStringInQuotes(String inputString) {
        return "\"" + inputString + "\"";
    }
}

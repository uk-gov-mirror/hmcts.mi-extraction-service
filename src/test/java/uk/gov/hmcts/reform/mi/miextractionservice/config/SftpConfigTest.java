package uk.gov.hmcts.reform.mi.miextractionservice.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class SftpConfigTest {

    private final SftpConfig classToTest = new SftpConfig();

    @Test
    void testJschCreation() {
        assertNotNull(classToTest.jsch(), "new Jsch expected");
    }
}

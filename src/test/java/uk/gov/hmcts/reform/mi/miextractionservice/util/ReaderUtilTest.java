package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class ReaderUtilTest {

    private ReaderUtil underTest;

    @BeforeEach
    public void setUp() {
        underTest = new ReaderUtil();
    }

    @Test
    public void givenMultiLineStringAsBytes_whenParseBytesAsString_returnStringList() {
        String multiLineString = "This String Has\nA Newline";

        List<String> result = underTest.readBytesAsStrings(multiLineString.getBytes());

        assertEquals("This String Has", result.get(0), "First line does not match expected.");
        assertEquals("A Newline", result.get(1), "Second line does not match expected.");
    }

    @Test
    public void givenEmptyString_whenParseBytesAsString_returnEmptyList() {
        List<String> result = underTest.readBytesAsStrings("".getBytes());

        assertEquals(0, result.size(), "Result size is not correct.");
    }
}

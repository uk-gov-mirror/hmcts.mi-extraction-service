package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ReaderUtil {

    private static final String NEWLINE_REGEX = "\\R";

    public List<String> readBytesAsStrings(byte[] data) {
        String dataAsString = new String(data, Charset.defaultCharset());

        if (dataAsString.isEmpty()) {
            return Collections.emptyList();
        }

        String[] dataAsLines = dataAsString.split(NEWLINE_REGEX);
        return Arrays.asList(dataAsLines);
    }
}

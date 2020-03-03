package uk.gov.hmcts.reform.mi.miextractionservice.test.helpers;

import uk.gov.hmcts.reform.mi.miextractionservice.lib.CSVWriterKeepAlive;

import java.io.Writer;

@SuppressWarnings({"checkstyle:AbbreviationAsWordInName","PMD.AvoidThrowingRawExceptionTypes"})
public class CSVWriterThrowExceptionStub extends CSVWriterKeepAlive {

    public CSVWriterThrowExceptionStub(Writer writer) {
        super(writer);
    }

    @Override
    public void writeNext(String[] nextLine) {
        throw new RuntimeException("Throws an exception.");
    }

    @Override
    public void writeNext(String[] nextLine, boolean applyQuotesToAll) {
        throw new RuntimeException("Throws an exception.");
    }
}

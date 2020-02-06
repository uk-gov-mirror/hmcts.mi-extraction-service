package uk.gov.hmcts.reform.mi.miextractionservice.component;

public interface DataParserComponent<T> {

    T parse(String data);
}

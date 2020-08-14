package uk.gov.hmcts.reform.mi.miextractionservice.domain;

public enum SourceTypeEnum {

    PREFIX("prefix"),
    EQUAL("equal");

    private final String typeValue;

    SourceTypeEnum(String typeValue) {
        this.typeValue = typeValue;
    }

    public String getSourceValue() {
        return this.typeValue;
    }
}

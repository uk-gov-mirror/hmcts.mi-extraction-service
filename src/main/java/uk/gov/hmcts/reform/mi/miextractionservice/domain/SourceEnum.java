package uk.gov.hmcts.reform.mi.miextractionservice.domain;

public enum SourceEnum {

    CORE_CASE_DATA("CoreCaseData"),
    NOTIFY("Notify"),
    UNKNOWN("Unknown");

    private final String sourceType;

    SourceEnum(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getValue() {
        return sourceType;
    }
}

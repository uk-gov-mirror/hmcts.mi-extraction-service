package uk.gov.hmcts.reform.mi.miextractionservice.domain;

public enum SourceEnum {

    CORE_CASE_DATA("CoreCaseData"),
    NOTIFY("Notify"),
    PAYMENT("Payment"),
    PAYMENT_HISTORY("PaymentHistory"),
    PAYMENT_ALLOCATION("PaymentAllocation"),
    PAYMENT_REMISSION("PaymentRemission"),
    PAYMENT_FEE("PaymentFee"),
    UNKNOWN("Unknown");

    private final String sourceType;

    SourceEnum(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getValue() {
        return sourceType;
    }
}

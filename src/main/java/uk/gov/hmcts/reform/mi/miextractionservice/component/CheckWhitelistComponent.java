package uk.gov.hmcts.reform.mi.miextractionservice.component;

public interface CheckWhitelistComponent {

    boolean isContainerWhitelisted(String name);
}

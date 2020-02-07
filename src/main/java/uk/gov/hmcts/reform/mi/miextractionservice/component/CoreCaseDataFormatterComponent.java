package uk.gov.hmcts.reform.mi.miextractionservice.component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;

public interface CoreCaseDataFormatterComponent<T> {

    T formatData(CoreCaseData data);
}

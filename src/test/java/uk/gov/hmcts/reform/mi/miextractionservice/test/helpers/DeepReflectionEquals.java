package uk.gov.hmcts.reform.mi.miextractionservice.test.helpers;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.mockito.ArgumentMatcher;

import java.io.Serializable;

@SuppressWarnings("PMD.ArrayIsStoredDirectly")
public class DeepReflectionEquals implements ArgumentMatcher<Object>, Serializable {

    private static long serialVersionUID = 987L;

    private final Object wanted;
    private final String[] excludeFields;

    public DeepReflectionEquals(Object wanted, String... excludeFields) {
        this.wanted = wanted;
        this.excludeFields = excludeFields;
    }

    @Override
    public boolean matches(Object actual) {
        return EqualsBuilder.reflectionEquals(wanted, actual,
            true, null, true, excludeFields);
    }

    @Override
    public String toString() {
        return "deepRefEq(" + wanted + ")";
    }
}

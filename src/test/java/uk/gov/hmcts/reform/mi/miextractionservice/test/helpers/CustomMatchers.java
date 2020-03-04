package uk.gov.hmcts.reform.mi.miextractionservice.test.helpers;

import org.mockito.ArgumentMatcher;

import static org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress;

public final class CustomMatchers {

    private CustomMatchers() {
        // Private constructor
    }

    public static <T> T deepRefEq(T value, String... excludeFields) {
        reportMatcher(new DeepReflectionEquals(value, excludeFields));
        return null;
    }

    private static void reportMatcher(ArgumentMatcher<?> matcher) {
        mockingProgress().getArgumentMatcherStorage().reportMatcher(matcher);
    }
}

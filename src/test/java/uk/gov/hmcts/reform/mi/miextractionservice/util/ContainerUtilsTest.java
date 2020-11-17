package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceTypeEnum;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.TooManyMethods") // Test class
@ExtendWith(SpringExtension.class)
class ContainerUtilsTest {

    @Test
    void givenSourceHasNoDelimiter_whenGetContainerPrefix_returnSourceWithDelimiter() {
        assertEquals("test-", ContainerUtils.getContainerPrefix("test"),
                     "Non-delimited source should return with delimiter.");
    }

    @Test
    void givenSourceHasPrefixTypeAndMatchesStartOfContainerName_whenCheckContainer_thenReturnTrue() {
        SourceProperties sourceProperties = SourceProperties.builder().type(SourceTypeEnum.PREFIX).build();
        assertTrue(ContainerUtils.checkContainerName("container-hello", "container", sourceProperties),
                   "Prefix type source should return true when matched against prefix.");
    }

    @Test
    void givenSourceHasPrefixTypeAndDoesNotMatchStartOfContainerName_whenCheckContainer_thenReturnFalse() {
        SourceProperties sourceProperties = SourceProperties.builder().type(SourceTypeEnum.PREFIX).build();
        assertFalse(ContainerUtils.checkContainerName("hello-container", "container", sourceProperties),
                   "Prefix type source should return false when no match against prefix.");
    }

    @Test
    void givenSourceHasEqualTypeAndDoesNotMatchExactContainerName_whenCheckContainer_thenReturnFalse() {
        SourceProperties sourceProperties = SourceProperties.builder().type(SourceTypeEnum.EQUAL).build();
        assertFalse(ContainerUtils.checkContainerName("container-exact", "container", sourceProperties),
                    "Equal type source should return false when no exact match.");
    }

    @Test
    void givenSourceHasEqualTypeAndDoesMatchExactContainerName_whenCheckContainer_thenReturnFalse() {
        SourceProperties sourceProperties = SourceProperties.builder().type(SourceTypeEnum.EQUAL).build();
        assertTrue(ContainerUtils.checkContainerName("container-match", "container-match", sourceProperties),
                   "Equal type source should return false when exact match.");
    }

    @Test
    void givenSourceHasNoTypeAndDoesNotMatchExactContainerName_whenCheckContainer_thenReturnFalse() {
        assertFalse(ContainerUtils.checkContainerName("container-world", "container-", SourceProperties.builder().build()),
                    "No type source should return false when only matched against prefix.");
    }

    @Test
    void givenSourceHasNoTypeAndDoesMatchExactContainerName_whenCheckContainer_thenReturnTrue() {
        assertTrue(ContainerUtils.checkContainerName("container-matches", "container-matches", SourceProperties.builder().build()),
                    "No type source should return true when exact match.");
    }

    @Test
    void givenEmptyList_whenCheckWhiteList_thenReturnTrue() {
        assertTrue(ContainerUtils.checkWhitelist(Collections.emptyList(), "anyString"),
                   "Empty whitelist should return true.");
    }

    @Test
    void givenMissingContainerNameToMatchInList_whenCheckWhiteList_thenReturnFalse() {
        assertFalse(ContainerUtils.checkWhitelist(Collections.singletonList("anything"), ""),
                    "Missing container name should return false.");
    }

    @Test
    void givenMatchingContainerNameInList_whenCheckWhiteList_thenReturnTrue() {
        assertTrue(ContainerUtils.checkWhitelist(Collections.singletonList("matching"), "matching"),
                   "Matching container name to whitelist item should return true.");
    }

    @Test
    void givenPatternMatchingContainerNameInList_whenCheckWhiteList_thenReturnTrue() {
        assertTrue(ContainerUtils.checkWhitelist(Collections.singletonList("^match.*"), "matchingpattern"),
                   "Matching container name to whitelist pattern should return true.");
    }

    @Test
    void givenNotMatchingContainerInList_whenCheckWhiteList_thenReturnFalse() {
        assertFalse(ContainerUtils.checkWhitelist(Collections.singletonList("notMatching"), "doesNotMatch"),
                    "Non-matching container name to whitelist should return false.");
    }
}

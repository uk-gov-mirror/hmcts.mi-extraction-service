package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class CheckWhitelistComponentImplTest {

    private static final String TEST_CONTAINER_NAME = "testContainer";

    private CheckWhitelistComponentImpl underTest;

    @BeforeEach
    public void setUp() {
        underTest = new CheckWhitelistComponentImpl();
    }

    @Test
    public void givenNoWhitelist_whenCheckWhitelist_thenReturnTrue() {
        ReflectionTestUtils.setField(underTest, "whiteList", Collections.emptyList());

        assertTrue(underTest.isContainerWhitelisted(TEST_CONTAINER_NAME), "Empty whitelist should return true.");
    }

    @Test
    public void givenWhitelistedContainer_whenCheckWhitelist_thenReturnTrue() {
        ReflectionTestUtils.setField(underTest, "whiteList", Collections.singletonList(TEST_CONTAINER_NAME));

        assertTrue(underTest.isContainerWhitelisted(TEST_CONTAINER_NAME), "Name matching whitelist should return true.");
    }

    @Test
    public void givenNotWhitelistedContainer_whenCheckWhitelist_thenReturnFalse() {
        ReflectionTestUtils.setField(underTest, "whiteList", Collections.singletonList("DifferentName"));

        assertFalse(underTest.isContainerWhitelisted(TEST_CONTAINER_NAME), "Name not matching whitelist should return false.");
    }
}

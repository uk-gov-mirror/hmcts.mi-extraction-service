package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.SendEmailComponent;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class EmailBlobUrlToTargetsComponentImplTest {

    private static final String TEST_BLOB_URL = "testBlobUrl";

    @Mock
    private SendEmailComponent sendEmailComponent;

    @InjectMocks
    private EmailBlobUrlToTargetsComponentImpl underTest;

    @Test
    public void givenUrls_whenEmailBlobUrlToTargets_thenCallEmailMethodForEachRecipient() {
        String targetOne = "targetOne";
        String targetTwo = "targetTwo";
        String targets = targetOne + "," + targetTwo;

        ReflectionTestUtils.setField(underTest, "targets", targets);

        underTest.sendBlobUrl(TEST_BLOB_URL);

        verify(sendEmailComponent, times(1)).sendEmail(eq(targetOne), anyString(), eq(TEST_BLOB_URL));
        verify(sendEmailComponent, times(1)).sendEmail(eq(targetTwo), anyString(), eq(TEST_BLOB_URL));
    }

    @Test
    public void givenNoTargets_whenEmailBlobUrlToTargets_thenDoNothing() {
        ReflectionTestUtils.setField(underTest, "targets", "");

        underTest.sendBlobUrl(TEST_BLOB_URL);

        verify(sendEmailComponent, never()).sendEmail(anyString(), anyString(), anyString());
    }
}

package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class SourceUtilTest {

    private SourceUtil underTest;

    @BeforeEach
    void setUp() {
        underTest = new SourceUtil();
    }

    @Test
    void givenCoreCaseDataSource_whenGetContainerName_thenReturnCcd() {
        assertEquals("ccd", underTest.getContainerName(SourceEnum.CORE_CASE_DATA), "Expected ccd for CoreCaseData source.");
    }

    @Test
    void givenNotifySource_whenGetContainerName_thenReturnNotify() {
        assertEquals("notify", underTest.getContainerName(SourceEnum.NOTIFY), "Expected notify for Notify source.");
    }

    @Test
    void givenPaymentHistorySource_whenGetContainerName_thenReturnPaymentHistory() {
        assertEquals("payment-history", underTest.getContainerName(SourceEnum.PAYMENT_HISTORY),
                     "Expected payment-history for Payment History source.");
    }

    @Test
    void givenPaymentAllocation_whenGetContainerName_thenReturnPaymentAllocation() {
        assertEquals("payment-allocation", underTest.getContainerName(SourceEnum.PAYMENT_ALLOCATION),
                     "Expected payment-allocation for Payment Allocation source.");
    }

    @Test
    void givenPaymentRemission_whenGetContainerName_thenReturnPaymentRemission() {
        assertEquals("payment-remission", underTest.getContainerName(SourceEnum.PAYMENT_REMISSION),
                     "Expected payment-remission for Payment Remission source.");
    }

    @Test
    void givenPaymentFee_whenGetContainerName_thenReturnPaymentFee() {
        assertEquals("payment-fee", underTest.getContainerName(SourceEnum.PAYMENT_FEE),
                     "Expected payment-fee for Payment Fee source.");
    }

    @Test
    void givenUnknownSource_whenGetContainerName_thenReturnUnknown() {
        assertEquals("unknown", underTest.getContainerName(SourceEnum.UNKNOWN), "Expected unknown for Unknown source.");
    }
}

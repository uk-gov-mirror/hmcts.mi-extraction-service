//package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import uk.gov.hmcts.reform.mi.micore.component.BlobDownloadComponent;
//import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
//import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
//import uk.gov.hmcts.reform.mi.miextractionservice.component.CsvWriterComponent;
//import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
//import uk.gov.hmcts.reform.mi.miextractionservice.component.EncryptArchiveComponent;
//import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
//import uk.gov.hmcts.reform.mi.miextractionservice.model.OutputCoreCaseData;
//import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;
//import uk.gov.hmcts.reform.mi.miextractionservice.util.ReaderUtil;
//
//@ExtendWith(SpringExtension.class)
//public class CoreCaseDataExportBlobDataComponentImplTest {
//
//    @Mock
//    private BlobDownloadComponent<byte[]> blobDownloadComponent;
//
//    @Mock
//    private DataParserComponent<CoreCaseData> dataParserComponent;
//
//    @Mock
//    private CoreCaseDataFormatterComponent<OutputCoreCaseData> coreCaseDataFormatterComponent;
//
//    @Mock
//    private CsvWriterComponent<OutputCoreCaseData> csvWriterComponent;
//
//    @Mock
//    private EncryptArchiveComponent encryptArchiveComponent;
//
//    @Mock
//    private GenerateBlobUrlComponent generateBlobUrlComponent;
//
//    @Mock
//    private ReaderUtil readerUtil;
//
//    @Mock
//    private DateTimeUtil dateTimeUtil;
//
//    @InjectMocks
//    private CoreCaseDataExportBlobDataComponentImpl underTest;
//
//    @Test
//    public void givenBlobServiceClientsAndDatesToExtract_whenExportBlobDataAndGetUrl_thenReturnUrlOfUploadedExtractedDataBlob() {
//
//    }
//}

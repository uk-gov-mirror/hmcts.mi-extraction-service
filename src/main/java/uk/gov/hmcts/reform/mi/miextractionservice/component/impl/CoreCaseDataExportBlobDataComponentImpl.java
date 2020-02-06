package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.component.BlobDownloadComponent;
import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CsvWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.EncryptArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.model.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.util.ReaderUtil;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CoreCaseDataExportBlobDataComponentImpl implements ExportBlobDataComponent {

    private static final String CCD_OUTPUT_CONTAINER_NAME = "ccd";
    private static final String NAME_DELIMITER = "-";
    private static final String CCD_DATA_CONTAINER_PREFIX = CCD_OUTPUT_CONTAINER_NAME + NAME_DELIMITER;
    private static final String WORKING_FILE_NAME = "CCD_EXTRACT.csv";
    private static final String WORKING_ARCHIVE = "CCD_EXTRACT.zip";

    @Autowired
    private BlobDownloadComponent<byte[]> blobDownloadComponent;

    @Autowired
    private DataParserComponent<CoreCaseData> dataParserComponent;

    @Autowired
    private CoreCaseDataFormatterComponent<OutputCoreCaseData> coreCaseDataFormatterComponent;

    @Autowired
    private CsvWriterComponent<OutputCoreCaseData> csvWriterComponent;

    @Autowired
    private  EncryptArchiveComponent encryptArchiveComponent;

    @Autowired
    private GenerateBlobUrlComponent generateBlobUrlComponent;

    @Autowired
    private ReaderUtil readerUtil;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Override
    public String exportBlobsAndReturnUrl(BlobServiceClient sourceBlobServiceClient,
                                          BlobServiceClient targetBlobServiceClient,
                                          OffsetDateTime fromDate,
                                          OffsetDateTime toDate) {

        List<OutputCoreCaseData> outputData = new ArrayList<>();

        for (BlobContainerItem blobContainerItem : sourceBlobServiceClient.listBlobContainers()) {
            if (blobContainerItem.getName().startsWith(CCD_DATA_CONTAINER_PREFIX)) {
                for (BlobItem blobItem : sourceBlobServiceClient.getBlobContainerClient(blobContainerItem.getName()).listBlobs()) {
                    if (getYearMonthIndexes(fromDate, toDate).parallelStream().anyMatch(blobItem.getName()::contains)) {
                        byte[] blobData = blobDownloadComponent
                            .downloadBlob(sourceBlobServiceClient, blobContainerItem.getName(), blobItem.getName());

                        List<String> stringData = readerUtil.readBytesAsStrings(blobData);

                        outputData.addAll(convertDataStringsToOutput(stringData, fromDate, toDate));
                    }
                }
            }
        }

        csvWriterComponent.writeBeansAsCsvFile(WORKING_FILE_NAME, outputData);

        encryptArchiveComponent.createEncryptedArchive(Collections.singletonList(WORKING_FILE_NAME), WORKING_ARCHIVE);

        BlobContainerClient blobContainerClient = targetBlobServiceClient.getBlobContainerClient(CCD_OUTPUT_CONTAINER_NAME);

        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }

        String outputBlobName = fromDate.format(dateTimeUtil.getDateFormat())
            + NAME_DELIMITER
            + toDate.format(dateTimeUtil.getDateFormat())
            + NAME_DELIMITER
            + WORKING_ARCHIVE;

        blobContainerClient.getBlobClient(outputBlobName).uploadFromFile(WORKING_ARCHIVE, true);

        return generateBlobUrlComponent.generateUrlForBlob(targetBlobServiceClient, CCD_OUTPUT_CONTAINER_NAME, outputBlobName);
    }

    private List<OutputCoreCaseData> convertDataStringsToOutput(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {
        List<CoreCaseData> coreCaseDataList = new ArrayList<>();
        data.forEach(string -> coreCaseDataList.add(dataParserComponent.parse(string)));

        List<OutputCoreCaseData> outputCoreCaseDataList = new ArrayList<>();
        coreCaseDataList.forEach(coreCaseData -> {
            OffsetDateTime eventCreatedDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(coreCaseData.getCeCreatedDate()), ZoneOffset.UTC);
            if (eventCreatedDate.isAfter(fromDate) && eventCreatedDate.isBefore(toDate)) {
                outputCoreCaseDataList.add(coreCaseDataFormatterComponent.formatData(coreCaseData));
            }
        });

        return outputCoreCaseDataList;
    }

    private List<String> getYearMonthIndexes(OffsetDateTime fromDate, OffsetDateTime toDate) {
        List<String> listOfIndexes = new ArrayList<>();

        for (int i = fromDate.getYear(); i <= toDate.getYear(); i++) {
            // For every month
            for (int j = 1; j <= 12; j++) {
                if (isYearAndMonthBetweenDates(i, j, fromDate, toDate)) {
                    String index = i + NAME_DELIMITER + dateTimeUtil.getFormattedMonthNumber(j);
                    listOfIndexes.add(index);
                }
            }
        }

        return listOfIndexes;
    }

    @SuppressWarnings("PMD.UselessParentheses")
    private boolean isYearAndMonthBetweenDates(int year, int month, OffsetDateTime fromDate, OffsetDateTime toDate) {
        return (year == fromDate.getYear() && month >= fromDate.getMonthValue())
            || (year > fromDate.getYear() && year < toDate.getYear())
            || (year == toDate.getYear() && month <= toDate.getMonthValue());

    }
}

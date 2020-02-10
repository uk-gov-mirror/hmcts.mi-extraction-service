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
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.util.ReaderUtil;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_DATA_CONTAINER_PREFIX;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_OUTPUT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_WORKING_ARCHIVE;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_WORKING_FILE_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NAME_DELIMITER;

@Component
public class CoreCaseDataExportBlobDataComponentImpl implements ExportBlobDataComponent {

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

    @SuppressWarnings("PMD.LawOfDemeter")
    @Override
    public String exportBlobsAndReturnUrl(BlobServiceClient sourceBlobServiceClient,
                                          BlobServiceClient targetBlobServiceClient,
                                          OffsetDateTime fromDate,
                                          OffsetDateTime toDate) {

        List<OutputCoreCaseData> outputData = new ArrayList<>();

        List<String> blobNameIndexes = dateTimeUtil.getListOfYearsAndMonthsBetweenDates(fromDate, toDate);

        for (BlobContainerItem blobContainerItem : sourceBlobServiceClient.listBlobContainers()) {
            if (blobContainerItem.getName().startsWith(CCD_DATA_CONTAINER_PREFIX)) {
                for (BlobItem blobItem : sourceBlobServiceClient.getBlobContainerClient(blobContainerItem.getName()).listBlobs()) {
                    if (blobNameIndexes.parallelStream().anyMatch(blobItem.getName()::contains)) {

                        byte[] blobData = blobDownloadComponent
                            .downloadBlob(sourceBlobServiceClient, blobContainerItem.getName(), blobItem.getName());

                        List<String> stringData = readerUtil.readBytesAsStrings(blobData);

                        List<CoreCaseData> filteredData = filterDataInDateRange(stringData, fromDate, toDate);
                        List<OutputCoreCaseData> formattedData = filteredData.stream()
                            .map(coreCaseDataFormatterComponent::formatData)
                            .collect(Collectors.toList());

                        outputData.addAll(formattedData);
                    }
                }
            }
        }

        if (Boolean.TRUE.equals(outputData.isEmpty())) {
            throw new ExportException("No data to output.");
        }

        csvWriterComponent.writeBeansAsCsvFile(CCD_WORKING_FILE_NAME, outputData);

        encryptArchiveComponent.createEncryptedArchive(Collections.singletonList(CCD_WORKING_FILE_NAME), CCD_WORKING_ARCHIVE);

        BlobContainerClient blobContainerClient = targetBlobServiceClient.getBlobContainerClient(CCD_OUTPUT_CONTAINER_NAME);

        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }

        String outputBlobName = fromDate.format(dateTimeUtil.getDateFormat())
            + NAME_DELIMITER
            + toDate.format(dateTimeUtil.getDateFormat())
            + NAME_DELIMITER
            + CCD_WORKING_ARCHIVE;

        blobContainerClient.getBlobClient(outputBlobName).uploadFromFile(CCD_WORKING_ARCHIVE, true);

        return generateBlobUrlComponent.generateUrlForBlob(targetBlobServiceClient, CCD_OUTPUT_CONTAINER_NAME, outputBlobName);
    }

    private List<CoreCaseData> filterDataInDateRange(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {
        return data
            .stream()
            .map(dataRow -> dataParserComponent.parse(dataRow))
            .filter(coreCaseData -> {
                OffsetDateTime eventCreatedDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(coreCaseData.getCeCreatedDate()), ZoneOffset.UTC);
                return eventCreatedDate.isEqual(fromDate) || eventCreatedDate.isEqual(toDate)
                    || eventCreatedDate.isAfter(fromDate) && eventCreatedDate.isBefore(toDate);
            })
            .collect(Collectors.toList());
    }
}

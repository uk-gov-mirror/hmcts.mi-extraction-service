package uk.gov.hmcts.reform.mi.miextractionservice.service.export;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.mi.miextractionservice.component.archive.ArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.compression.CompressionComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.notification.NotifyTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.sftp.SftpExportComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.writer.DataWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.ExportProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.azure.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.util.ContainerUtils;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateUtils;
import uk.gov.hmcts.reform.mi.miextractionservice.util.FileUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.parseBoolean;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.DASH_DELIMITER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.GZIP_EXTENSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.JSONL_EXTENSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.NEWLINE_DELIMITER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.ZIP_EXTENSION;

@Slf4j
@AllArgsConstructor
@Service
public class ExportServiceImpl implements ExportService {

    private final @Value("${compression.enabled}") String compressionEnabled;
    private final @Value("${archive.enabled}") String archiveEnabled;
    private final @Value("${mail.enabled}") String mailEnabled;
    private final @Value("${container-whitelist}") List<String> containerWhitelist;
    private final @Value("${retrieve-from-date}") String retrieveFromDate;
    private final @Value("${retrieve-to-date}") String retrieveToDate;
    private final ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;
    private final ExportProperties exportProperties;
    private final DataWriterComponent dataWriterComponent;
    private final CompressionComponent compressionComponent;
    private final ArchiveComponent archiveComponent;
    private final NotifyTargetsComponent notifyTargetsComponent;
    private final SftpExportComponent sftpExportComponent;

    @Override
    public void exportData() {
        BlobServiceClient stagingClient = extractionBlobServiceClientFactory.getStagingClient();
        BlobServiceClient exportClient = extractionBlobServiceClientFactory.getExportClient();

        checkStorageConnection(); // To fail early if unable to connect to storage accounts.

        List<String> messages = new ArrayList<>();

        exportProperties.getSources().forEach(
            (source, properties) -> {
                if (properties.isEnabled()) {
                    try {
                        exportDataForSource(stagingClient, exportClient, source, properties, messages);
                    } catch (Exception e) {
                        log.error("Unable to parse data for source {}. Skipping.", source, e);
                    }
                }
            }
        );

        if (parseBoolean(mailEnabled)) {
            notifyTargetsComponent.sendMessage(String.join(NEWLINE_DELIMITER, messages));
        }
    }

    @SuppressWarnings("squid:S899")
    @Override
    public void checkStorageConnection() {
        extractionBlobServiceClientFactory
            .getStagingClient()
            .listBlobContainers()
            .iterator()
            .hasNext();

        extractionBlobServiceClientFactory
            .getExportClient()
            .listBlobContainers()
            .iterator()
            .hasNext();
    }

    private void exportDataForSource(BlobServiceClient stagingClient,
                                     BlobServiceClient exportClient,
                                     String source,
                                     SourceProperties properties,
                                     List<String> messages) {

        final LocalDate fromDate = DateUtils.getRetrievalDate(retrieveFromDate);
        final LocalDate toDate = DateUtils.getRetrievalDate(retrieveToDate);
        final List<String> datesToParse = DateUtils.getListOfYearsAndMonthsBetweenDates(fromDate, toDate);

        log.info("Beginning export of {} data for date range {} to {}", source, fromDate.format(ISO_DATE), toDate.format(ISO_DATE));

        final List<BlobContainerItem> containersToParse = stagingClient.listBlobContainers().stream()
            .filter(container -> ContainerUtils.checkWhitelist(containerWhitelist, container.getName()))
            .filter(container -> ContainerUtils.checkContainerName(container.getName(), source, properties))
            .collect(Collectors.toList());

        int totalRecords = 0;

        String fileName = getExportName(source, fromDate, toDate, JSONL_EXTENSION);

        try (BufferedWriter writer = FileUtils.openBufferedWriter(fileName)) {
            for (BlobContainerItem containerItem : containersToParse) {
                totalRecords += parseContainerForData(stagingClient, writer, containerItem.getName(),
                                                      properties, fromDate, toDate, datesToParse);
            }
        } catch (IOException e) {
            throw new ExportException("Exception occurred when writing data for source: " + source, e);
        }

        if (totalRecords > 0) {
            uploadFileToBlobStore(exportClient, source, properties, fromDate, toDate);

            log.info("Uploaded total of {} records for source {} with file name {}", totalRecords, source, fileName);

            messages.add(String.format("Blob %s uploaded to container %s.", fileName, source));
        } else {
            log.info("Nothing to upload for source {} in date range {} to {}", source, fromDate.format(ISO_DATE), toDate.format(ISO_DATE));
        }
        FileUtils.deleteFile(fileName);
    }

    private int parseContainerForData(BlobServiceClient serviceClient,
                                      BufferedWriter writer,
                                      String containerName,
                                      SourceProperties properties,
                                      LocalDate fromDate,
                                      LocalDate toDate,
                                      List<String> datesToParse) {

        final BlobContainerClient blobContainerClient = serviceClient.getBlobContainerClient(containerName);

        int totalRecords = 0;

        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            if (datesToParse.parallelStream().anyMatch(blobItem.getName()::contains)) {
                totalRecords += dataWriterComponent.writeRecordsForDateRange(writer, blobContainerClient.getBlobClient(blobItem.getName()),
                                                                             properties, fromDate, toDate);
            }
        }

        return totalRecords;
    }

    private void uploadFileToBlobStore(BlobServiceClient blobServiceClient,
                                       String source,
                                       SourceProperties properties,
                                       LocalDate fromDate,
                                       LocalDate toDate) {

        String fileName = getExportName(source, fromDate, toDate, JSONL_EXTENSION);
        String gzipName = getExportName(source, fromDate, toDate, JSONL_EXTENSION + GZIP_EXTENSION);
        String zipName = getExportName(source, fromDate, toDate, ZIP_EXTENSION);

        try {
            if (parseBoolean(compressionEnabled)) {
                compressionComponent.compressFile(fileName, gzipName);
            }

            String workingFileName = parseBoolean(compressionEnabled) ? gzipName : fileName;

            if (parseBoolean(archiveEnabled)) {
                archiveComponent.createArchive(Collections.singletonList(workingFileName), zipName);
            }

            String blobName = parseBoolean(archiveEnabled) ? zipName : workingFileName;

            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(getExportContainerName(
                source,
                properties
            ));
            if (FALSE.equals(blobContainerClient.exists())) {
                blobContainerClient.create();
            }

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            blobClient.uploadFromFile(blobName, true);
            sftpExportComponent.copyFile(blobName, properties.getSftpDir());
        } finally {
            // Clean up
            FileUtils.deleteFile(fileName);
            FileUtils.deleteFile(gzipName);
            FileUtils.deleteFile(zipName);
        }
    }

    private String getExportName(String source, LocalDate fromDate, LocalDate toDate, String extension) {
        return source
            + DASH_DELIMITER
            + fromDate.format(ISO_DATE)
            + DASH_DELIMITER
            + toDate.format(ISO_DATE)
            + extension;
    }

    private String getExportContainerName(String source, SourceProperties properties) {
        return Optional.ofNullable(properties.getPrefix())
            .map(prefix -> prefix + DASH_DELIMITER)
            .orElse("") + source;
    }
}

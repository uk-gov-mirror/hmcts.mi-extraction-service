package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobDownloadComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CheckWhitelistComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CsvWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.MetadataFilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.WriterWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_DATA_CONTAINER_PREFIX;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_OUTPUT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_WORKING_ARCHIVE;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_WORKING_FILE_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NAME_DELIMITER;

@Slf4j
@Component
public class CoreCaseDataExportBlobDataComponentImpl implements ExportBlobDataComponent {

    @Value("${max-lines-buffer}")
    private String maxLines;

    @Autowired
    private WriterWrapper writerWrapper;

    @Autowired
    private CheckWhitelistComponent checkWhitelistComponent;

    @Autowired
    private MetadataFilterComponent metadataFilterComponent;

    @Autowired
    private BlobDownloadComponent blobDownloadComponent;

    @Autowired
    private FilterComponent<CoreCaseData> filterComponent;

    @Autowired
    private CoreCaseDataFormatterComponent<OutputCoreCaseData> coreCaseDataFormatterComponent;

    @Autowired
    private CsvWriterComponent<OutputCoreCaseData> csvWriterComponent;

    @Autowired
    private ArchiveComponent archiveComponent;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    /**
     * Exports data matching date range provided as a CSV, compressed in an encrypted archive for ease of upload and download and security.
     *
     * @param sourceBlobServiceClient the blob service client of the source storage account.
     * @param targetBlobServiceClient the blob service client of the target storage account.
     * @param fromDate the first date in yyyy-MM-dd format to pull data for.
     * @param toDate the last date in yyyy-MM-dd format to pull data for.
     * @return String name of the generated archive stored as a blob on the storage account.
     */
    @SuppressWarnings("PMD.LawOfDemeter")
    @Override
    public String exportBlobsAndGetOutputName(BlobServiceClient sourceBlobServiceClient,
                                              BlobServiceClient targetBlobServiceClient,
                                              OffsetDateTime fromDate,
                                              OffsetDateTime toDate) {

        boolean dataFound = readAndWriteDataAsCsv(sourceBlobServiceClient, fromDate, toDate);

        if (dataFound) {
            archiveComponent.createArchive(Collections.singletonList(CCD_WORKING_FILE_NAME), CCD_WORKING_ARCHIVE);

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

            return outputBlobName;
        }

        log.warn("No data found to output.");
        return null;
    }

    private boolean readAndWriteDataAsCsv(BlobServiceClient sourceBlobServiceClient,
                                          OffsetDateTime fromDate,
                                          OffsetDateTime toDate) {
        boolean dataFound = false;

        List<String> blobNameIndexes = dateTimeUtil.getListOfYearsAndMonthsBetweenDates(fromDate, toDate);

        try (BufferedWriter bufferedWriter = writerWrapper.getBufferedWriter(Paths.get(CCD_WORKING_FILE_NAME))) {

            csvWriterComponent.writeHeadersToCsvFile(bufferedWriter);

            for (BlobContainerItem blobContainerItem : sourceBlobServiceClient.listBlobContainers()) {

                if (checkWhitelistComponent.isContainerWhitelisted(blobContainerItem.getName())
                    && blobContainerItem.getName().startsWith(CCD_DATA_CONTAINER_PREFIX)) {

                    BlobContainerClient blobContainerClient = sourceBlobServiceClient.getBlobContainerClient(blobContainerItem.getName());

                    for (BlobItem blobItem : blobContainerClient.listBlobs()) {
                        if (blobNameIndexes.parallelStream().anyMatch(blobItem.getName()::contains)
                            && metadataFilterComponent.filterByMetadata(blobItem.getMetadata())) {

                            // Once dataFound is true, stays true for the rest of the run.
                            dataFound = parseAndWriteData(
                                bufferedWriter,
                                sourceBlobServiceClient,
                                blobContainerItem.getName(),
                                blobItem.getName(),
                                fromDate,
                                toDate
                            ) || dataFound;
                        }
                    }
                }
            }
        } catch (IOException exception) {
            log.error("Exception occurred initialising file for writing.");
            throw new ParserException("Unable create file for writing.", exception);
        }

        return dataFound;
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    private boolean parseAndWriteData(BufferedWriter bufferedWriter,
                                      BlobServiceClient sourceBlobServiceClient,
                                      String blobContainerName,
                                      String blobName,
                                      OffsetDateTime fromDate,
                                      OffsetDateTime toDate) {

        boolean dataFound = false;

        try (InputStream inputStream = blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, blobContainerName, blobName);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line = bufferedReader.readLine();

            List<String> outputLines = new ArrayList<>();

            while (line != null) {
                if (StringUtils.isNotBlank(line)) {
                    outputLines.add(line);
                    dataFound = true;

                    // Reached max buffer, so write to file chunk and clear
                    if (outputLines.size() >= Integer.parseInt(maxLines)) {
                        writeDataToCsv(bufferedWriter, outputLines, fromDate, toDate);
                        outputLines.clear();
                    }
                }

                line = bufferedReader.readLine();
            }

            if (Boolean.FALSE.equals(outputLines.isEmpty())) {
                writeDataToCsv(bufferedWriter, outputLines, fromDate, toDate);
            }
        } catch (IOException exception) {
            log.error("Exception occurred writing data from blob to file.");
            throw new ParserException("Unable to parse or write data to file.", exception);
        }

        return dataFound;
    }

    private void writeDataToCsv(BufferedWriter writer, List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {
        List<CoreCaseData> filteredData = filterComponent.filterDataInDateRange(data, fromDate, toDate);
        List<OutputCoreCaseData> formattedData = filteredData.stream()
            .map(coreCaseDataFormatterComponent::formatData)
            .collect(Collectors.toList());

        csvWriterComponent.writeBeansWithWriter(writer, formattedData);
    }
}

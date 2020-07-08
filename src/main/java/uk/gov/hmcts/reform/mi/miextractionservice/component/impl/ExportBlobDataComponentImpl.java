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

import uk.gov.hmcts.reform.mi.miextractionservice.component.ArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobDownloadComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CheckWhitelistComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.MetadataFilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.WriteDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.WriteDataFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.util.SourceUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.FileWrapper;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.WriterWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.DOT_DELIMITER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NAME_DELIMITER;

@Slf4j
@Component
public class ExportBlobDataComponentImpl implements ExportBlobDataComponent {

    @Value("${max-lines-buffer}")
    private String maxLines;

    @Value("${archive.compression.enabled}")
    private String archiveFlag;

    @Autowired
    private WriterWrapper writerWrapper;

    @Autowired
    private FileWrapper fileWrapper;

    @Autowired
    private CheckWhitelistComponent checkWhitelistComponent;

    @Autowired
    private MetadataFilterComponent metadataFilterComponent;

    @Autowired
    private BlobDownloadComponent blobDownloadComponent;

    @Autowired
    private WriteDataFactory writeDataFactory;

    @Autowired
    private ArchiveComponent archiveComponent;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Autowired
    private SourceUtil sourceUtil;

    /**
     * Exports data matching date range provided as a JsonL, compressed in an encrypted archive for ease of upload and download and security.
     *
     * @param sourceBlobServiceClient the blob service client of the source storage account.
     * @param targetBlobServiceClient the blob service client of the target storage account.
     * @param fromDate the first date in yyyy-MM-dd format to pull data for.
     * @param toDate the last date in yyyy-MM-dd format to pull data for.
     * @param fileName the fileName of the output.
     * @return String name of the generated archive stored as a blob on the storage account.
     */
    @SuppressWarnings("PMD.LawOfDemeter")
    @Override
    public String exportBlobsAndGetOutputName(BlobServiceClient sourceBlobServiceClient,
                                              BlobServiceClient targetBlobServiceClient,
                                              OffsetDateTime fromDate,
                                              OffsetDateTime toDate,
                                              String fileName,
                                              SourceEnum source) {

        String outputDatePrefix = fromDate.format(dateTimeUtil.getDateFormat())
            + NAME_DELIMITER
            + toDate.format(dateTimeUtil.getDateFormat())
            + NAME_DELIMITER;

        String workingFileName = outputDatePrefix + fileName;

        int dataCount = readAndWriteData(sourceBlobServiceClient, fromDate, toDate, workingFileName, source);

        if (dataCount > 0) {
            log.info("Found a total of {} records to write for {}. About to upload blob.", dataCount, source.getValue());

            BlobContainerClient blobContainerClient = targetBlobServiceClient.getBlobContainerClient(sourceUtil.getContainerName(source));

            if (!blobContainerClient.exists()) {
                blobContainerClient.create();
            }

            String outputBlobName = workingFileName;

            if (Boolean.TRUE.equals(Boolean.parseBoolean(archiveFlag))) {
                String fileExtension = fileName.substring(fileName.lastIndexOf(DOT_DELIMITER));
                String archiveName = fileName.replace(fileExtension, ".zip");

                outputBlobName = outputDatePrefix + archiveName;

                archiveComponent.createArchive(Collections.singletonList(workingFileName), outputBlobName);

                fileWrapper.deleteFileOnExit(workingFileName);
            }

            blobContainerClient.getBlobClient(outputBlobName).uploadFromFile(outputBlobName, true);

            log.info("Uploaded in container {}, blob {} for {}.", sourceUtil.getContainerName(source), outputBlobName, source.getValue());

            fileWrapper.deleteFileOnExit(outputBlobName);

            return outputBlobName;
        }

        log.warn("No data found to output.");
        return null;
    }

    private int readAndWriteData(BlobServiceClient sourceBlobServiceClient,
                                     OffsetDateTime fromDate,
                                     OffsetDateTime toDate,
                                     String workingFileName,
                                     SourceEnum source) {
        int dataCount = 0;

        List<String> blobNameIndexes = dateTimeUtil.getListOfYearsAndMonthsBetweenDates(fromDate, toDate);

        try (BufferedWriter bufferedWriter = writerWrapper.getBufferedWriter(Paths.get(workingFileName))) {

            for (BlobContainerItem blobContainerItem : sourceBlobServiceClient.listBlobContainers()) {

                if (checkWhitelistComponent.isContainerWhitelisted(blobContainerItem.getName())
                    && blobContainerItem.getName().startsWith(sourceUtil.getContainerName(source).concat("-"))) {

                    BlobContainerClient blobContainerClient = sourceBlobServiceClient.getBlobContainerClient(blobContainerItem.getName());

                    for (BlobItem blobItem : blobContainerClient.listBlobs()) {
                        if (blobNameIndexes.parallelStream().anyMatch(blobItem.getName()::contains)
                            && metadataFilterComponent.filterByMetadata(blobItem.getMetadata())) {

                            log.info("Container {} with Blob {} meets {} source criteria. Parsing for data.",
                                blobContainerItem.getName(), blobItem.getName(), source.getValue());

                            int addedRecordsCount = parseAndWriteData(
                                bufferedWriter,
                                sourceBlobServiceClient,
                                blobContainerItem.getName(),
                                blobItem.getName(),
                                fromDate,
                                toDate,
                                source
                            );

                            dataCount = dataCount + addedRecordsCount;
                        }
                    }
                }
            }
        } catch (IOException exception) {
            log.error("Exception occurred initialising file for writing.");
            throw new ParserException("Unable create file for writing.", exception);
        }

        return dataCount;
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    private int parseAndWriteData(BufferedWriter bufferedWriter,
                                      BlobServiceClient sourceBlobServiceClient,
                                      String blobContainerName,
                                      String blobName,
                                      OffsetDateTime fromDate,
                                      OffsetDateTime toDate,
                                      SourceEnum source) {

        int dataCount = 0;

        try (InputStream inputStream = blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, blobContainerName, blobName);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line = bufferedReader.readLine();

            List<String> outputLines = new ArrayList<>();

            while (line != null) {
                if (StringUtils.isNotBlank(line)) {
                    outputLines.add(line);

                    // Reached max buffer, so write to file chunk and clear
                    if (outputLines.size() >= Integer.parseInt(maxLines)) {
                        dataCount = dataCount + writeData(bufferedWriter, outputLines, fromDate, toDate, source);
                        outputLines.clear();
                    }
                }

                line = bufferedReader.readLine();
            }

            if (Boolean.FALSE.equals(outputLines.isEmpty())) {
                dataCount = dataCount + writeData(bufferedWriter, outputLines, fromDate, toDate, source);
            }
        } catch (IOException exception) {
            log.error("Exception occurred writing data from blob to file.");
            throw new ParserException("Unable to parse or write data to file.", exception);
        }

        return dataCount;
    }

    private int writeData(BufferedWriter writer, List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate, SourceEnum source) {
        WriteDataComponent writeDataComponent = writeDataFactory.getWriteComponent(source);
        return writeDataComponent.writeData(writer, data, fromDate, toDate);
    }
}

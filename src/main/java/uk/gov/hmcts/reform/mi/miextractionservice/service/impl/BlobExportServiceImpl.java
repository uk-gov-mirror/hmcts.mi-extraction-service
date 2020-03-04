package uk.gov.hmcts.reform.mi.miextractionservice.service.impl;

import com.azure.storage.blob.BlobServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SasIpWhitelist;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.service.BlobExportService;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import java.time.OffsetDateTime;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_OUTPUT_CONTAINER_NAME;

@Service
public class BlobExportServiceImpl implements BlobExportService {

    private static final String MESSAGE_DELIMITER = " : ";
    private static final String MESSAGE_NEWLINE_DELIMITER = "\n\n";
    private static final String LOCATION_DELIMITER = "-";
    private static final String SPACE_DELIMITER = " ";

    @Value("${retrieve-from-date}")
    private String retrieveFromDate;

    @Value("${retrieve-to-date}")
    private String retrieveToDate;

    @Autowired
    private SasIpWhitelist sasIpWhitelist;

    @Autowired
    private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;

    @Autowired
    private ExportBlobDataComponent exportBlobDataComponent;

    @Autowired
    private GenerateBlobUrlComponent generateBlobUrlComponent;

    @Autowired
    private EmailBlobUrlToTargetsComponent sendBlobUrlToTargetsComponent;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Override
    public void exportBlobs() {
        OffsetDateTime fromDate = StringUtils.isEmpty(retrieveFromDate)
            ? getStartOfDay(dateTimeUtil.getCurrentDateTime().minusDays(7L)) : dateTimeUtil.parseDateString(retrieveFromDate);

        OffsetDateTime toDate  = StringUtils.isEmpty(retrieveToDate)
            ? getEndOfDay(dateTimeUtil.getCurrentDateTime().minusDays(1L)) : getEndOfDay(dateTimeUtil.parseDateString(retrieveToDate));

        BlobServiceClient exportClient = extractionBlobServiceClientFactory.getExportClient();

        String outputBlobName = exportBlobDataComponent.exportBlobsAndGetOutputName(
            extractionBlobServiceClientFactory.getStagingClient(),
            exportClient,
            fromDate,
            toDate);

        String message = "";

        if (sasIpWhitelist.getRange().isEmpty()) {
            message = generateBlobUrlComponent.generateUrlForBlob(exportClient, CCD_OUTPUT_CONTAINER_NAME, outputBlobName);
        } else {
            for (String key : sasIpWhitelist.getRange().keySet()) {
                String locationName = key.replaceAll(LOCATION_DELIMITER, SPACE_DELIMITER);

                message = message.concat(locationName + MESSAGE_DELIMITER + generateBlobUrlComponent
                    .generateUrlForBlobWithIpRange(exportClient, CCD_OUTPUT_CONTAINER_NAME, outputBlobName, sasIpWhitelist.getRange().get(key)));

                message = message.concat(MESSAGE_NEWLINE_DELIMITER);
            }
        }

        sendBlobUrlToTargetsComponent.sendBlobUrl(message);
    }

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

    private OffsetDateTime getStartOfDay(OffsetDateTime offsetDateTime) {
        return offsetDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private OffsetDateTime getEndOfDay(OffsetDateTime offsetDateTime) {
        return offsetDateTime.withHour(23).withMinute(59).withSecond(59).withNano(999);
    }
}

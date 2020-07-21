package uk.gov.hmcts.reform.mi.miextractionservice.service.impl;

import com.azure.storage.blob.BlobServiceClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobMessageBuilderComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.service.BlobExportService;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_OUTPUT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_WORKING_FILE_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.EXTRACT_FILE_NAME_SUFFIX;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NEWLINE_DELIMITER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_OUTPUT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_WORKING_FILE_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NO_FILTER_VALUE;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.PAYMENT_ALLOCATION_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.PAYMENT_FEE_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.PAYMENT_HISTORY_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.PAYMENT_REMISSION_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum.CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum.NOTIFY;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum.PAYMENT;

@Service
public class BlobExportServiceImpl implements BlobExportService {

    @Value("${retrieve-from-date}")
    private String retrieveFromDate;

    @Value("${retrieve-to-date}")
    private String retrieveToDate;

    @Value("${filter.data-source}")
    private String dataSource;

    @Autowired
    private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;

    @Autowired
    private ExportBlobDataComponent exportBlobDataComponent;

    @Autowired
    private BlobMessageBuilderComponent blobMessageBuilderComponent;

    @Autowired
    private EmailBlobUrlToTargetsComponent sendBlobUrlToTargetsComponent;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @SuppressWarnings("PMD.LawOfDemeter")
    @Override
    public void exportBlobs() {
        OffsetDateTime fromDate = StringUtils.isEmpty(retrieveFromDate)
            ? getStartOfDay(dateTimeUtil.getCurrentDateTime().minusDays(1L)) : dateTimeUtil.parseDateString(retrieveFromDate);

        OffsetDateTime toDate  = StringUtils.isEmpty(retrieveToDate)
            ? getEndOfDay(dateTimeUtil.getCurrentDateTime().minusDays(1L)) : getEndOfDay(dateTimeUtil.parseDateString(retrieveToDate));

        BlobServiceClient stagingClient = extractionBlobServiceClientFactory.getStagingClient();
        BlobServiceClient exportClient = extractionBlobServiceClientFactory.getExportClient();

        List<String> messages = new ArrayList<>();

        exportData(stagingClient, exportClient, fromDate, toDate, messages);

        if (!messages.isEmpty()) {
            sendBlobUrlToTargetsComponent.sendBlobUrl(messages.stream().collect(Collectors.joining(NEWLINE_DELIMITER)));
        }
    }

    private void exportData(BlobServiceClient stagingClient, BlobServiceClient exportClient, OffsetDateTime fromDate, OffsetDateTime toDate,
                            List<String> messages) {

        if (dataSource.equalsIgnoreCase(NO_FILTER_VALUE) || dataSource.equalsIgnoreCase(CORE_CASE_DATA.getValue())) {
            exportCcdData(stagingClient, exportClient, fromDate, toDate, messages);
        }

        if (dataSource.equalsIgnoreCase(NO_FILTER_VALUE) || dataSource.equalsIgnoreCase(NOTIFY.getValue())) {
            exportNotifyData(stagingClient, exportClient, fromDate, toDate, messages);
        }

        if (dataSource.equalsIgnoreCase(NO_FILTER_VALUE) || dataSource.equalsIgnoreCase(PAYMENT.getValue())) {
            exportPaymentData(stagingClient, exportClient, fromDate, toDate, messages);
        }
    }

    private void exportCcdData(BlobServiceClient stagingClient, BlobServiceClient exportClient, OffsetDateTime fromDate, OffsetDateTime toDate,
                               List<String> messages) {

        String outputBlobName = exportBlobDataComponent.exportBlobsAndGetOutputName(
            stagingClient,
            exportClient,
            fromDate,
            toDate,
            CCD_WORKING_FILE_NAME,
            CORE_CASE_DATA
        );

        if (outputBlobName != null) {
            messages.add(blobMessageBuilderComponent.buildMessage(exportClient, CCD_OUTPUT_CONTAINER_NAME, outputBlobName));
        }
    }

    private void exportNotifyData(BlobServiceClient stagingClient, BlobServiceClient exportClient, OffsetDateTime fromDate, OffsetDateTime toDate,
                                  List<String> messages) {

        String notifyOutputBlobName = exportBlobDataComponent.exportBlobsAndGetOutputName(
            stagingClient,
            exportClient,
            fromDate,
            toDate,
            NOTIFY_WORKING_FILE_NAME,
            NOTIFY
        );

        if (notifyOutputBlobName != null) {
            messages.add(blobMessageBuilderComponent.buildMessage(exportClient, NOTIFY_OUTPUT_CONTAINER_NAME, notifyOutputBlobName));
        }
    }

    private void exportPaymentData(BlobServiceClient stagingClient, BlobServiceClient exportClient, OffsetDateTime fromDate, OffsetDateTime toDate,
                                   List<String> messages) {

        List<String> blobNames = new ArrayList<>();

        blobNames.add(exportBlobDataComponent.exportBlobsAndGetOutputName(
            stagingClient,
            exportClient,
            fromDate,
            toDate,
            PAYMENT_HISTORY_NAME + EXTRACT_FILE_NAME_SUFFIX,
            SourceEnum.PAYMENT_HISTORY
        ));

        blobNames.add(exportBlobDataComponent.exportBlobsAndGetOutputName(
            stagingClient,
            exportClient,
            fromDate,
            toDate,
            PAYMENT_ALLOCATION_NAME + EXTRACT_FILE_NAME_SUFFIX,
            SourceEnum.PAYMENT_ALLOCATION
        ));

        blobNames.add(exportBlobDataComponent.exportBlobsAndGetOutputName(
            stagingClient,
            exportClient,
            fromDate,
            toDate,
            PAYMENT_REMISSION_NAME + EXTRACT_FILE_NAME_SUFFIX,
            SourceEnum.PAYMENT_REMISSION
        ));

        blobNames.add(exportBlobDataComponent.exportBlobsAndGetOutputName(
            stagingClient,
            exportClient,
            fromDate,
            toDate,
            PAYMENT_FEE_NAME + EXTRACT_FILE_NAME_SUFFIX,
            SourceEnum.PAYMENT_FEE
        ));

        blobNames.stream()
            .filter(StringUtils::isNotEmpty)
            .forEach(blobName -> messages.add(blobMessageBuilderComponent.buildMessage(exportClient, "payment", blobName)));

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

    private OffsetDateTime getStartOfDay(OffsetDateTime offsetDateTime) {
        return offsetDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private OffsetDateTime getEndOfDay(OffsetDateTime offsetDateTime) {
        return offsetDateTime.withHour(23).withMinute(59).withSecond(59).withNano(999);
    }
}

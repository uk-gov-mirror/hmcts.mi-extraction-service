package uk.gov.hmcts.reform.mi.miextractionservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.service.BlobExportService;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import java.time.OffsetDateTime;

@Service
public class BlobExportServiceImpl implements BlobExportService {

    @Value("${retrieve-from-date}")
    private String retrieveFromDate;

    @Value("${retrieve-to-date}")
    private String retrieveToDate;

    @Autowired
    private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;

    @Autowired
    private ExportBlobDataComponent exportBlobDataComponent;

    @Autowired
    private EmailBlobUrlToTargetsComponent sendBlobUrlToTargetsComponent;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Override
    public void exportBlobs() {
        OffsetDateTime fromDate = StringUtils.isEmpty(retrieveFromDate)
            ? getMidnight(dateTimeUtil.getCurrentDateTime().minusDays(7L)) : dateTimeUtil.parseDateString(retrieveFromDate);

        OffsetDateTime toDate  = StringUtils.isEmpty(retrieveToDate)
            ? dateTimeUtil.getCurrentDateTime() : dateTimeUtil.parseDateString(retrieveToDate).plusDays(1L);

        String url = exportBlobDataComponent.exportBlobsAndReturnUrl(
            extractionBlobServiceClientFactory.getStagingClient(),
            extractionBlobServiceClientFactory.getExportClient(),
            fromDate,
            toDate);

        sendBlobUrlToTargetsComponent.sendBlobUrl(url);
    }

    private OffsetDateTime getMidnight(OffsetDateTime offsetDateTime) {
        return offsetDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
}

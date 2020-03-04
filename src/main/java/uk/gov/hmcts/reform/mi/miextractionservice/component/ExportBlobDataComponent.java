package uk.gov.hmcts.reform.mi.miextractionservice.component;

import com.azure.storage.blob.BlobServiceClient;

import java.time.OffsetDateTime;

public interface ExportBlobDataComponent {

    String exportBlobsAndGetOutputName(BlobServiceClient sourceBlobServiceClient,
                                       BlobServiceClient targetBlobServiceClient,
                                       OffsetDateTime fromDate,
                                       OffsetDateTime toDate);
}

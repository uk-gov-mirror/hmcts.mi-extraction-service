package uk.gov.hmcts.reform.mi.miextractionservice.domain;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Builder;
import lombok.Value;

@SuppressWarnings("PMD.TooManyFields")
@Builder
@Value
public class CsvNotificationOutput {

    @CsvBindByPosition(position = 0)
    private String extractionDate;

    @CsvBindByPosition(position = 1)
    private String id;

    @CsvBindByPosition(position = 2)
    private String service;

    @CsvBindByPosition(position = 3)
    private String reference;

    @CsvBindByPosition(position = 4)
    private String emailAddress;

    @CsvBindByPosition(position = 5)
    private String phoneNumber;

    @CsvBindByPosition(position = 6)
    private String lineOne;

    @CsvBindByPosition(position = 7)
    private String lineTwo;

    @CsvBindByPosition(position = 8)
    private String lineThree;

    @CsvBindByPosition(position = 9)
    private String lineFour;

    @CsvBindByPosition(position = 10)
    private String lineFive;

    @CsvBindByPosition(position = 11)
    private String lineSix;

    @CsvBindByPosition(position = 12)
    private String postcode;

    @CsvBindByPosition(position = 13)
    private String type;

    @CsvBindByPosition(position = 14)
    private String status;

    @CsvBindByPosition(position = 15)
    private String templateId;

    @CsvBindByPosition(position = 16)
    private String templateVersion;

    @CsvBindByPosition(position = 17)
    private String templateUri;

    @CsvBindByPosition(position = 18)
    private String templateName;

    @CsvBindByPosition(position = 19)
    private String body;

    @CsvBindByPosition(position = 20)
    private String subject;

    @CsvBindByPosition(position = 21)
    private String createdAt;

    @CsvBindByPosition(position = 22)
    private String sentAt;

    @CsvBindByPosition(position = 23)
    private String completedAt;
}

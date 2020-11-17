# mi-extraction-service
MI Phase Two Extraction Service

[![Build Status](https://dev.azure.com/hmcts/MI%20Reporting/_apis/build/status/hmcts.mi-extraction-service?branchName=master)](https://dev.azure.com/hmcts/MI%20Reporting/_build/latest?definitionId=298&branchName=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Ami-extraction-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=uk.gov.hmcts.reform%3Ami-extraction-service)
Extraction service will pull all data for a specified source within a specified date range. Defaults to one days worth of data from midnight the day before to just before midnight the day the service is run.
Extraction service works by checking all monthly blobs for the specified source containers that are within the specified date range and then writing to a new file all the json records that land within that date range.
This means that the date partition field must be specified in the configuration as well.
Currently, same date range extractions will be overridden by new runs.

Extraction service will be the service to use in order to change data formats or push data to specified locations.
By default the output blobs will be stored onto the extraction storage.

Extraction service will also handle any required encryption, compression or archiving.

### Environment variables
The following generic environment variables are required:

| Name | Default | Description |
|------|---------|-------------|
|APPINSIGHTS_INSTRUMENTATIONKEY||Azure App Instrumentation key|
|MI_CLIENT_ID||Managed Identity client id|
|STORAGE_STAGING_NAME||Persistent storage name|
|STORAGE_STAGING_CONNECTION_STRING||Connection string as alternative to Managed Identity|
|STORAGE_EXPORT_NAME||Export storage name|
|STORAGE_EXPORT_CONNECTION_STRING||Export storage connection string as alternative to Managed Identity|
|CONTAINER_WHITELIST||Filter containers to search through for export|
|GOV_UK_NOTIFY_API_KEY||Gov UK Notify API Key for the account to be used to send out notifications|
|MAIL_TARGETS||Comma separated list of emails to send notifications to|
|RETRIEVE_FROM_DATE||The date in yyyy-MM-dd format to start retrieving data from|
|RETRIEVE_TO_DATE||The date in yyyy-MM-dd format to stop retrieving data on|

### Additional environment variables

| Name | Default | Description |
|------|---------|-------------|
|COMPRESSION_ENABLED|true|Whether output should be gzipped before stored as blob|
|ARCHIVE_ENABLED|false|Whether output should be archived into as .zip before being stored as a blob|
|ARCHIVE_ENCRYPTION_ENABLED|false|Whether the .zip should be password protected. Requires ARCHIVE_ENABLED and password to be set|
|ARCHIVE_ENCRYPTION_PASSWORD||Required is ARCHIVE_ENCRYPTION_ENABLED is true. The password to lock the .zip with|

### Configuration

All export is setup via configuration in the application.yaml.

The current structure looks like for example:

    name:
      enabled: boolean
      date-field: string
      timezone: java.time.ZoneId string
      type: string

| Name | Description |
|------|-------------|
|name|The name of the container(s) to extract data from|
|enabled|The flag to determine if extraction is run for the configuration.
|date-field|The json field name to be used for extracting by date|
|timezone|Optional; defaults to UTC. Sets another timezone to parse the date-field in|
|type|Optional; defaults to exact name match. Can also be set as prefix to parse all containers prefixed with the name and a - delimiter|

### Pipeline

The project is currently deployed onto a kubernetes cluster via azure pipelines.
You can find the azure-pipelines.yml detailing the build steps that occur when code is pushed to the repository.
The azure-pipelines-scheduled.yml will run on a daily schedule for dependency checks and mutation testing.

The main step definitions can be found in the common [mi-core-lib](https://github.com/hmcts/mi-core-lib).

As a Kubernetes CronJob type service, the code is tested in the pipeline via local helm charts.
The actual release will be controlled by flux charts in another repository. To test locally use the charts found in the charts folder.

## Plugins for code quality

The service contains the following plugins:

  * checkstyle

    https://docs.gradle.org/current/userguide/checkstyle_plugin.html

    Performs code style checks on Java source files using Checkstyle and generates reports from these checks.
    The checks are included in gradle's *check* task (you can run them by executing `./gradlew check` command).

  * pmd

    https://docs.gradle.org/current/userguide/pmd_plugin.html

    Performs static code analysis to finds common programming flaws. Included in gradle `check` task.

  * org.owasp.dependencycheck

    https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html

    Provides monitoring of the project's dependent libraries and creating a report
    of known vulnerable components that are included in the build. To run it
    execute `gradle dependencyCheckAnalyze` command.

  * org.sonarqube

    https://sonarcloud.io

    Provides sonar code analysis, code coverage based on jacoco test reports and quality gates.
    Runs as part of the pipeline and uploads the report to the organisation sonarcloud.

  * info.solidsoft.pitest

    https://pitest.org/

    Provides mutation testing. Run as part of the scheduled pipeline as enabled by the -scheduled pipeline.yml.

### Prerequisites
Open JDK 11

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

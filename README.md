### Management Information Extraction Service

This service will handle retrieving data from the Staging storage and processing it so that it may be sent to the end user.

This service will run as a Kubernetes CronJob and will run daily or weekly as requested.

## Whitelisting IPs

To whitelist an IP range when using the BlobSasMessageBuilderComponent, add the following to the application.properties:

sas-ip-whitelist.range.name-name.name = 100.100.0.0-100.200.0.255

(In the name part, the name is automatically converted to title case and - and . are replaced with whitespaces.)

Or as an environment variable in the form:

SASIPWHITELIST_RANGE_NAME_NAME_NAME: 100.100.0.0-100.200.0.255

(In the name part, the name is automatically converted to title case and the _ is replaced with whitespaces.)
# BotFramework Connector

> see https://aka.ms/autorest

Configuration for generating BotFramework Connector SDK.

``` yaml
add-credentials: true
openapi-type: data-plane
```
The current release for the BotFramework Connector is v3.0.

# Releases

## Connector API 3.0

``` yaml
input-file: ConnectorAPI.json
directive:
  from: ConnectorAPI.json
  where: $..['x-ms-enum']
  transform: >
    if( $['modelAsString'] ) {
      $['modelAsString'] = false;
    }
```

### Connector API 3.0 - Java Settings
These settings apply only when `--java` is specified on the command line.
``` yaml $(java)
java:
  override-client-name: ConnectorClient
  license-header: MICROSOFT_MIT_NO_VERSION
  azure-arm: true
  use-internal-constructors: true
  namespace: com.microsoft.bot.schema
  output-folder: ./generated
```
# Trino SharePoint Connector

A Trino connector for querying SharePoint data using SQL.

## Quick Start (Development)

The fastest way to develop and test the SharePoint connector is using Trino's development server, which loads plugins directly from source without manual copying.

### Build

```bash
# Build SharePoint connector and development server
./mvnw clean install -DskipTests -pl :trino-sharepoint,:trino-server-dev,:trino-cli -am
```

### Configuration

The development server is already configured to load the SharePoint plugin at `testing/trino-server-dev/etc/config.properties`.

Create your SharePoint catalog configuration:

**`testing/trino-server-dev/etc/catalog/sharepoint.properties`**
```properties
connector.name=sharepoint
sharepoint.site-url=https://example.sharepoint.com
```

### Start Server

```bash
./mvnw exec:java -pl :trino-server-dev \
  -Dexec.mainClass="io.trino.server.DevelopmentServer" \
  -Dexec.args="--etc-dir=testing/trino-server-dev/etc"
```

Wait for the message: `======== SERVER STARTED ========`

## Query

```bash
# Show available catalogs
java -jar client/trino-cli/target/trino-cli-477-executable.jar \
  --server localhost:8080 \
  --execute "SHOW CATALOGS;"

# Show SharePoint schemas
java -jar client/trino-cli/target/trino-cli-477-executable.jar \
  --server localhost:8080 \
  --execute "SHOW SCHEMAS FROM sharepoint;"

# Interactive mode
java -jar client/trino-cli/target/trino-cli-477-executable.jar \
  --server localhost:8080
```

## Configuration Properties

| Property | Description | Required | Default |
|----------|-------------|----------|---------|
| `sharepoint.site-url` | SharePoint site URL | Yes | - |

## Production Deployment

For production deployment using the full Trino server distribution:

```bash
# Build full server with all plugins
./mvnw clean install -DskipTests -pl :trino-server,:trino-cli -am

# Copy SharePoint plugin to server distribution
cp -r plugin/trino-sharepoint/target/trino-sharepoint-446 \
  core/trino-server/target/trino-server-446/plugin/sharepoint

# Start server
core/trino-server/target/trino-server-446/bin/launcher run --etc-dir=etc
```

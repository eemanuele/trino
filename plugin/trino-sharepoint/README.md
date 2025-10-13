# Trino SharePoint Connector

A Trino connector for querying SharePoint data using SQL.

## Build

```bash
# Build minimal components: SharePoint connector, Trino server, and CLI
./mvnw clean install -DskipTests -pl :trino-sharepoint,:trino-server,:trino-cli -am

# Copy plugin to server
cp -r plugin/trino-sharepoint/target/trino-sharepoint-446 \
  core/trino-server/target/trino-server-446/plugin/sharepoint
```

## Configuration

Create configuration files in the `etc/` directory at the repository root:

### etc/config.properties
```properties
coordinator=true
node-scheduler.include-coordinator=true
http-server.http.port=8080
discovery.uri=http://localhost:8080
```

### etc/node.properties
```properties
node.environment=development
node.id=trino-dev
node.internal-address=localhost
node.data-dir=/tmp/trino-data
```

### etc/jvm.config
```
-server
-Xmx4G
-XX:+UseG1GC
-XX:+ExitOnOutOfMemoryError
```

### etc/catalog/sharepoint.properties
```properties
connector.name=sharepoint
sharepoint.site-url=https://example.sharepoint.com
```

## Start Server

```bash
core/trino-server/target/trino-server-446/bin/launcher run --etc-dir=etc
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

# Trino SharePoint Connector

A Trino connector for querying SharePoint data using SQL.

## Development Setup

### Build

```bash
# Build SharePoint connector, CLI, and Trino server
./mvnw clean install -DskipTests -pl :trino-sharepoint,:trino-server,:trino-cli -am
```

### Install Plugin

```bash
# Copy SharePoint plugin to server distribution
cp -r plugin/trino-sharepoint/target/trino-sharepoint-446 \
  core/trino-server/target/trino-server-446/plugin/sharepoint
```

### Configuration

The configuration files are already set up in `etc/`:

**`etc/node.properties`**
```properties
node.environment=test
node.id=ffffffff-ffff-ffff-ffff-ffffffffffff
node.internal-address=localhost
```

**`etc/config.properties`** - Server configuration with minimal settings for development

**`etc/catalog/sharepoint.properties`** - Update with your SharePoint site URL:
```properties
connector.name=sharepoint
sharepoint.site-url=https://your-site.sharepoint.com
```

### Start Server

```bash
core/trino-server/target/trino-server-446/bin/launcher run --etc-dir=etc
```

Wait for startup to complete, then the server will be available at `http://localhost:8080`

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

## Notes

### Why not use DevelopmentServer?

The `DevelopmentServer` approach (using `trino-server-dev` with Maven-based plugin loading) depends on `io.airlift.resolver` which includes Maven 3.0.4 as a transitive dependency. Maven 3.0.4 is from 2012 and has known security vulnerabilities that may be flagged by security scanning tools.

For this reason, this guide uses the production server approach with pre-built plugins, which avoids the problematic dependency while still providing a fast development workflow.

# Trino SharePoint Connector

A Trino connector for querying SharePoint data using SQL.

## Development Setup

### Build

The provisio configuration has been modified to only include TPCH and SharePoint plugins for faster builds.

```bash
# Build SharePoint connector, server, and CLI
./mvnw clean install -DskipTests -pl :trino-sharepoint,:trino-server,:trino-cli -am
```

This builds only the minimal plugins needed for development (TPCH for testing, SharePoint for your connector).

### Configuration

The configuration files are already set up in `etc/`:

**`etc/node.properties`**
```properties
node.environment=test
node.id=ffffffff-ffff-ffff-ffff-ffffffffffff
node.internal-address=localhost
```

**`etc/config.properties`** - Server configuration with minimal settings for development (concurrent startup, HTTP port 8080, discovery URI, timeouts)

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

In a new terminal:

```bash
# Show available catalogs
java -jar client/trino-cli/target/trino-cli-446-executable.jar \
  --server localhost:8080 \
  --execute "SHOW CATALOGS;"

# Show SharePoint schemas
java -jar client/trino-cli/target/trino-cli-446-executable.jar \
  --server localhost:8080 \
  --execute "SHOW SCHEMAS FROM sharepoint;"

# Interactive mode
java -jar client/trino-cli/target/trino-cli-446-executable.jar \
  --server localhost:8080
```

In interactive mode, try:
```sql
SHOW CATALOGS;
USE sharepoint.default;
SHOW TABLES;
```

## Configuration Properties

| Property | Description | Required | Default |
|----------|-------------|----------|---------|
| `sharepoint.site-url` | SharePoint site URL | Yes | - |

## Notes

### Modified provisio configuration

The `core/trino-server/src/main/provisio/trino.xml` file has been modified to only include TPCH and SharePoint plugins. This significantly reduces build time by avoiding compilation of all Trino plugins.

### Why not use DevelopmentServer?

The `DevelopmentServer` approach (using `trino-server-dev` with Maven-based plugin loading) depends on `io.airlift.resolver` which includes Maven 3.0.4 as a transitive dependency. Maven 3.0.4 is from 2012 and has known security vulnerabilities that may be flagged by security scanning tools.

### Development workflow

After the initial build, you can rebuild just your SharePoint plugin:

```bash
# Rebuild SharePoint plugin only (fast!)
./mvnw clean install -DskipTests -pl :trino-sharepoint

# Copy updated plugin
cp -r plugin/trino-sharepoint/target/trino-sharepoint-446 \
  core/trino-server/target/trino-server-446/plugin/sharepoint

# Restart server
core/trino-server/target/trino-server-446/bin/launcher restart --etc-dir=etc
```

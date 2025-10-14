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

Set up the configuration files in `testing/trino-server-dev/etc/`:

**`testing/trino-server-dev/etc/node.properties`**
```properties
node.environment=test
node.id=ffffffff-ffff-ffff-ffff-ffffffffffff
node.internal-address=localhost
```

**`testing/trino-server-dev/etc/config.properties`**
```properties
#
# WARNING
# ^^^^^^^
# This configuration file is for development only and should NOT be used
# in production. For example configuration, see the Trino documentation.
#

# sample nodeId to provide consistency across test runs
node.id=ffffffff-ffff-ffff-ffff-ffffffffffff
node.environment=test
node.internal-address=localhost
experimental.concurrent-startup=true
http-server.http.port=8080

discovery.uri=http://localhost:8080

exchange.http-client.max-connections-per-server=1000
exchange.http-client.connect-timeout=1m
exchange.http-client.idle-timeout=1m

scheduler.http-client.max-connections-per-server=1000
scheduler.http-client.connect-timeout=1m
scheduler.http-client.idle-timeout=1m

query.client.timeout=5m
query.min-expire-age=30m

plugin.bundles=\
  plugin/trino-tpch/pom.xml, \
  plugin/trino-sharepoint/pom.xml

node-scheduler.include-coordinator=true
```

**`testing/trino-server-dev/etc/jvm.config`**
```properties
#
# WARNING
# ^^^^^^^
# This configuration file is for development only and should NOT be used
# in production. For example configuration, see the Trino documentation.
#
```

**`testing/trino-server-dev/etc/log.properties`**
```properties
#
# WARNING
# ^^^^^^^
# This configuration file is for development only and should NOT be used
# in production. For example configuration, see the Trino documentation.
#

io.trino=INFO

# show classpath for plugins
io.trino.server.PluginManager=DEBUG

# Maven plugin loading code
com.ning.http.client=WARN
```

**`testing/trino-server-dev/etc/access-control.properties`**
```properties
access-control.name=default
```

**`testing/trino-server-dev/etc/catalog/sharepoint.properties`**
```properties
connector.name=sharepoint
sharepoint.site-url=https://example.sharepoint.com
```

### Start Server

```bash
./mvnw exec:java -pl :trino-server-dev \
  -Dexec.mainClass="io.trino.server.DevelopmentServer" \
  -Dexec.systemProperties \
  -Dnode.environment=test \
  -Dnode.id=ffffffff-ffff-ffff-ffff-ffffffffffff \
  "-Dplugin.bundles=plugin/trino-tpch/pom.xml,plugin/trino-sharepoint/pom.xml"
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

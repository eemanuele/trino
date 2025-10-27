/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.sharepoint;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ColumnMetadata;
import io.trino.spi.connector.ConnectorMetadata;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTableMetadata;
import io.trino.spi.connector.SchemaTableName;
import io.trino.spi.type.VarcharType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

public class SharePointMetadata
        implements ConnectorMetadata
{
    private final SharePointConfig config;
    private final SharePointClient client;

    @Inject
    public SharePointMetadata(SharePointConfig config, SharePointClient client)
    {
        this.config = requireNonNull(config, "config is null");
        this.client = requireNonNull(client, "client is null");
    }

    @Override
    public List<String> listSchemaNames(ConnectorSession session)
    {
        return ImmutableList.of("default");
    }

    @Override
    public List<SchemaTableName> listTables(ConnectorSession session, Optional<String> schemaName)
    {
        try {
            List<SharePointClient.SharePointList> lists = client.getLists();
            return lists.stream()
                    .map(list -> new SchemaTableName("default", list.name))
                    .collect(toImmutableList());
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to list SharePoint tables", e);
        }
    }

    @Override
    public ConnectorTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName)
    {
        if (!tableName.getSchemaName().equals("default")) {
            return null;
        }

        try {
            List<SharePointClient.SharePointList> lists = client.getLists();
            for (SharePointClient.SharePointList list : lists) {
                if (list.name.equals(tableName.getTableName())) {
                    return new SharePointTableHandle(tableName.getSchemaName(), tableName.getTableName(), list.id);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to get table handle", e);
        }

        return null;
    }

    @Override
    public ConnectorTableMetadata getTableMetadata(ConnectorSession session, ConnectorTableHandle table)
    {
        SharePointTableHandle tableHandle = (SharePointTableHandle) table;

        try {
            List<Map<String, Object>> columns = client.getColumns(tableHandle.listId());

            ImmutableList.Builder<ColumnMetadata> columnMetadata = ImmutableList.builder();
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("name");
                // Map SharePoint types to Trino types (simplified for now)
                columnMetadata.add(new ColumnMetadata(columnName, VarcharType.VARCHAR));
            }

            return new ConnectorTableMetadata(
                    new SchemaTableName(tableHandle.schemaName(), tableHandle.tableName()),
                    columnMetadata.build());
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to get table metadata", e);
        }
    }

    @Override
    public Map<String, ColumnHandle> getColumnHandles(ConnectorSession session, ConnectorTableHandle tableHandle)
    {
        SharePointTableHandle handle = (SharePointTableHandle) tableHandle;

        try {
            List<Map<String, Object>> columns = client.getColumns(handle.listId());

            ImmutableMap.Builder<String, ColumnHandle> columnHandles = ImmutableMap.builder();
            for (int i = 0; i < columns.size(); i++) {
                Map<String, Object> column = columns.get(i);
                String columnName = (String) column.get("name");
                columnHandles.put(columnName, new SharePointColumnHandle(columnName, VarcharType.VARCHAR, i));
            }

            return columnHandles.buildOrThrow();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to get column handles", e);
        }
    }

    @Override
    public ColumnMetadata getColumnMetadata(
            ConnectorSession session,
            ConnectorTableHandle tableHandle,
            ColumnHandle columnHandle)
    {
        return ((SharePointColumnHandle) columnHandle).toColumnMetadata();
    }
}

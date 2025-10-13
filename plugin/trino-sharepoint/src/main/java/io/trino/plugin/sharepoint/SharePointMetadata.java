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
import io.trino.spi.type.BigintType;
import io.trino.spi.type.VarcharType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SharePointMetadata
        implements ConnectorMetadata
{
    private final SharePointConfig config;

    @Inject
    public SharePointMetadata(SharePointConfig config)
    {
        this.config = requireNonNull(config, "config is null");
    }

    @Override
    public List<String> listSchemaNames(ConnectorSession session)
    {
        return ImmutableList.of("default");
    }

    @Override
    public List<SchemaTableName> listTables(ConnectorSession session, Optional<String> schemaName)
    {
        // Return an example table
        return ImmutableList.of(new SchemaTableName("default", "documents"));
    }

    @Override
    public ConnectorTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName)
    {
        if (!tableName.getSchemaName().equals("default")) {
            return null;
        }
        if (!tableName.getTableName().equals("documents")) {
            return null;
        }
        return new SharePointTableHandle(tableName.getSchemaName(), tableName.getTableName());
    }

    @Override
    public ConnectorTableMetadata getTableMetadata(ConnectorSession session, ConnectorTableHandle table)
    {
        SharePointTableHandle tableHandle = (SharePointTableHandle) table;

        // Define example columns
        List<ColumnMetadata> columns = ImmutableList.of(
                new ColumnMetadata("id", BigintType.BIGINT),
                new ColumnMetadata("name", VarcharType.VARCHAR),
                new ColumnMetadata("created", VarcharType.VARCHAR));

        return new ConnectorTableMetadata(
                new SchemaTableName(tableHandle.schemaName(), tableHandle.tableName()),
                columns);
    }

    @Override
    public Map<String, ColumnHandle> getColumnHandles(ConnectorSession session, ConnectorTableHandle tableHandle)
    {
        return ImmutableMap.of(
                "id", new SharePointColumnHandle("id", BigintType.BIGINT, 0),
                "name", new SharePointColumnHandle("name", VarcharType.VARCHAR, 1),
                "created", new SharePointColumnHandle("created", VarcharType.VARCHAR, 2));
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

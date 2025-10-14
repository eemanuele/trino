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

import com.google.inject.Inject;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorRecordSetProvider;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplit;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.RecordCursor;
import io.trino.spi.connector.RecordSet;
import io.trino.spi.type.Type;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class SharePointRecordSetProvider
        implements ConnectorRecordSetProvider
{
    private final SharePointClient client;

    @Inject
    public SharePointRecordSetProvider(SharePointClient client)
    {
        this.client = client;
    }

    @Override
    public RecordSet getRecordSet(
            ConnectorTransactionHandle transaction,
            ConnectorSession session,
            ConnectorSplit split,
            ConnectorTableHandle table,
            List<? extends ColumnHandle> columns)
    {
        SharePointTableHandle tableHandle = (SharePointTableHandle) table;
        List<SharePointColumnHandle> sharePointColumns = columns.stream()
                .map(SharePointColumnHandle.class::cast)
                .collect(toImmutableList());

        return new RecordSet()
        {
            @Override
            public List<Type> getColumnTypes()
            {
                return sharePointColumns.stream()
                        .map(SharePointColumnHandle::type)
                        .collect(toImmutableList());
            }

            @Override
            public RecordCursor cursor()
            {
                try {
                    List<java.util.Map<String, Object>> items = client.getListItems(tableHandle.listId());
                    return new SharePointRecordCursor(sharePointColumns, items);
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to fetch SharePoint data", e);
                }
            }
        };
    }
}

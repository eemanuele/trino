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
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.trino.spi.connector.RecordCursor;
import io.trino.spi.type.Type;

import java.util.List;
import java.util.Map;

public class SharePointRecordCursor
        implements RecordCursor
{
    private final List<SharePointColumnHandle> columns;
    private final List<Map<String, Object>> rows;
    private int currentRow = -1;

    public SharePointRecordCursor(List<SharePointColumnHandle> columns, List<Map<String, Object>> items)
    {
        this.columns = columns;
        this.rows = items;
    }

    @Override
    public long getCompletedBytes()
    {
        return 0;
    }

    @Override
    public long getReadTimeNanos()
    {
        return 0;
    }

    @Override
    public Type getType(int field)
    {
        return columns.get(field).type();
    }

    @Override
    public boolean advanceNextPosition()
    {
        currentRow++;
        return currentRow < rows.size();
    }

    @Override
    public boolean getBoolean(int field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(int field)
    {
        SharePointColumnHandle column = columns.get(field);
        Map<String, Object> item = rows.get(currentRow);
        Map<String, Object> fields = (Map<String, Object>) item.get("fields");
        Object value = fields.get(column.name());
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    @Override
    public double getDouble(int field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Slice getSlice(int field)
    {
        SharePointColumnHandle column = columns.get(field);
        Map<String, Object> item = rows.get(currentRow);
        Map<String, Object> fields = (Map<String, Object>) item.get("fields");
        Object value = fields.get(column.name());
        if (value == null) {
            return Slices.utf8Slice("");
        }
        return Slices.utf8Slice(String.valueOf(value));
    }

    @Override
    public Object getObject(int field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNull(int field)
    {
        SharePointColumnHandle column = columns.get(field);
        Map<String, Object> item = rows.get(currentRow);
        Map<String, Object> fields = (Map<String, Object>) item.get("fields");
        return fields.get(column.name()) == null;
    }

    @Override
    public void close()
    {
    }
}

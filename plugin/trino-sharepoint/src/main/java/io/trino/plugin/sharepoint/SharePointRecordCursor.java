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

public class SharePointRecordCursor
        implements RecordCursor
{
    private final List<SharePointColumnHandle> columns;
    private final List<List<Object>> rows;
    private int currentRow = -1;

    public SharePointRecordCursor(List<SharePointColumnHandle> columns)
    {
        this.columns = columns;

        // Example data - 3 rows
        this.rows = ImmutableList.of(
                ImmutableList.of(1L, "Project Proposal.docx", "2025-01-15"),
                ImmutableList.of(2L, "Meeting Notes.docx", "2025-02-20"),
                ImmutableList.of(3L, "Budget 2025.xlsx", "2025-03-10"));
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
        return (Long) rows.get(currentRow).get(field);
    }

    @Override
    public double getDouble(int field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Slice getSlice(int field)
    {
        return Slices.utf8Slice((String) rows.get(currentRow).get(field));
    }

    @Override
    public Object getObject(int field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNull(int field)
    {
        return rows.get(currentRow).get(field) == null;
    }

    @Override
    public void close()
    {
    }
}

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SharePointClient
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final SharePointConfig config;
    private final SharePointAuthClient authClient;
    private final HttpClient httpClient;
    private String siteId;

    @Inject
    public SharePointClient(SharePointConfig config, SharePointAuthClient authClient)
    {
        this.config = requireNonNull(config, "config is null");
        this.authClient = requireNonNull(authClient, "authClient is null");
        this.httpClient = HttpClient.newBuilder().build();
    }

    private synchronized String getSiteId()
            throws IOException, InterruptedException
    {
        if (siteId == null) {
            // Extract hostname and site path from site URL
            URI siteUri = URI.create(config.getSiteUrl());
            String hostname = siteUri.getHost();
            String sitePath = siteUri.getPath();

            String url = String.format("https://graph.microsoft.com/v1.0/sites/%s:%s", hostname, sitePath);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authClient.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to get site ID: " + response.body());
            }

            SiteResponse siteResponse = OBJECT_MAPPER.readValue(response.body(), SiteResponse.class);
            this.siteId = siteResponse.id;
        }
        return siteId;
    }

    public List<SharePointList> getLists()
            throws IOException, InterruptedException
    {
        String url = String.format("https://graph.microsoft.com/v1.0/sites/%s/lists", getSiteId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authClient.getAccessToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get lists: " + response.body());
        }

        ListsResponse listsResponse = OBJECT_MAPPER.readValue(response.body(), ListsResponse.class);
        return listsResponse.value;
    }

    public List<Map<String, Object>> getColumns(String listId)
            throws IOException, InterruptedException
    {
        String url = String.format("https://graph.microsoft.com/v1.0/sites/%s/lists/%s/columns", getSiteId(), listId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authClient.getAccessToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get columns: " + response.body());
        }

        ColumnsResponse columnsResponse = OBJECT_MAPPER.readValue(response.body(), ColumnsResponse.class);
        return columnsResponse.value;
    }

    public List<Map<String, Object>> getListItems(String listId)
            throws IOException, InterruptedException
    {
        String url = String.format("https://graph.microsoft.com/v1.0/sites/%s/lists/%s/items?expand=fields", getSiteId(), listId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authClient.getAccessToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get list items: " + response.body());
        }

        ItemsResponse itemsResponse = OBJECT_MAPPER.readValue(response.body(), ItemsResponse.class);
        return itemsResponse.value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SiteResponse
    {
        @JsonProperty
        public String id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListsResponse
    {
        @JsonProperty
        public List<SharePointList> value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SharePointList
    {
        @JsonProperty
        public String id;

        @JsonProperty
        public String name;

        @JsonProperty
        public String displayName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ColumnsResponse
    {
        @JsonProperty
        public List<Map<String, Object>> value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemsResponse
    {
        @JsonProperty
        public List<Map<String, Object>> value;
    }
}

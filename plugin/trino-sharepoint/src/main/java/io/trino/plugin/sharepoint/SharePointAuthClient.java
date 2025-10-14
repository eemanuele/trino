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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static java.util.Objects.requireNonNull;

public class SharePointAuthClient
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final SharePointConfig config;
    private final HttpClient httpClient;

    private String accessToken;
    private Instant tokenExpiry;

    @Inject
    public SharePointAuthClient(SharePointConfig config)
    {
        this.config = requireNonNull(config, "config is null");
        this.httpClient = HttpClient.newBuilder().build();
    }

    public synchronized String getAccessToken()
            throws IOException, InterruptedException
    {
        if (accessToken == null || Instant.now().isAfter(tokenExpiry)) {
            refreshAccessToken();
        }
        return accessToken;
    }

    private void refreshAccessToken()
            throws IOException, InterruptedException
    {
        String tokenEndpoint = String.format(
                "https://login.microsoftonline.com/%s/oauth2/v2.0/token",
                config.getTenantId());

        String requestBody = String.format(
                "client_id=%s&scope=%s&client_secret=%s&grant_type=client_credentials",
                config.getClientId(),
                "https://graph.microsoft.com/.default",
                config.getClientSecret());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get access token: " + response.body());
        }

        TokenResponse tokenResponse = OBJECT_MAPPER.readValue(response.body(), TokenResponse.class);
        this.accessToken = tokenResponse.accessToken;
        this.tokenExpiry = Instant.now().plusSeconds(tokenResponse.expiresIn - 300); // Refresh 5 min early
    }

    private static class TokenResponse
    {
        @JsonProperty("access_token")
        public String accessToken;

        @JsonProperty("expires_in")
        public long expiresIn;
    }
}

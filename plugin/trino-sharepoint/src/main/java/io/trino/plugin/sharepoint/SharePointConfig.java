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

import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigDescription;
import jakarta.validation.constraints.NotNull;

public class SharePointConfig
{
    private String siteUrl;
    private String tenantId;
    private String clientId;
    private String clientSecret;

    @NotNull
    public String getSiteUrl()
    {
        return siteUrl;
    }

    @Config("sharepoint.site-url")
    @ConfigDescription("SharePoint site URL")
    public SharePointConfig setSiteUrl(String siteUrl)
    {
        this.siteUrl = siteUrl;
        return this;
    }

    @NotNull
    public String getTenantId()
    {
        return tenantId;
    }

    @Config("sharepoint.tenant-id")
    @ConfigDescription("Azure AD tenant ID")
    public SharePointConfig setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
        return this;
    }

    @NotNull
    public String getClientId()
    {
        return clientId;
    }

    @Config("sharepoint.client-id")
    @ConfigDescription("Azure AD application (client) ID")
    public SharePointConfig setClientId(String clientId)
    {
        this.clientId = clientId;
        return this;
    }

    @NotNull
    public String getClientSecret()
    {
        return clientSecret;
    }

    @Config("sharepoint.client-secret")
    @ConfigDescription("Azure AD application client secret")
    public SharePointConfig setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
        return this;
    }
}

/*
 * Copyright 2020 SmartBear Software Inc.
 *
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
 *
 * ########################################################################
 *
 * Copyright 2025 Ludy87
 *
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
package io.github.ludy87.swagger.swaggerhub.v2.client;

import java.io.IOException;

import org.gradle.api.GradleException;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Getter
@Builder
public class SwaggerHubClient {
    private static final String DOWNLOAD_FAILED_ERROR = "Failed to download API definition: ";
    private static final String UPLOAD_FAILED_ERROR = "Failed to upload API definition: ";
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String APIS = "apis";

    @NonNull private final String host;
    @NonNull private final String protocol;
    private final String token;

    private final int port;
    private final Boolean onPremise;
    private final String onPremiseAPISuffix;
    private final OkHttpClient client;

    public static SwaggerHubClient create(
            String host, Integer port, String protocol, String token) {
        return SwaggerHubClient.builder()
                .host(host)
                .port(port)
                .protocol(protocol)
                .token(token)
                .onPremise(false)
                .onPremiseAPISuffix(null)
                .client(CLIENT)
                .build();
    }

    public static SwaggerHubClient createOnPremise(
            String host,
            Integer port,
            String protocol,
            String token,
            Boolean onPremise,
            String onPremiseAPISuffix) {
        SwaggerHubClient swaggweHubClient =
                SwaggerHubClient.builder()
                        .host(host)
                        .port(port)
                        .protocol(protocol)
                        .token(token)
                        .onPremise(onPremise != null ? onPremise : false)
                        .onPremiseAPISuffix(onPremiseAPISuffix != null ? onPremiseAPISuffix : "v1")
                        .client(CLIENT)
                        .build();
        return swaggweHubClient;
    }

    public String getDefinition(SwaggerHubRequest swaggerHubRequest) throws GradleException {
        HttpUrl httpUrl = getDownloadUrl(swaggerHubRequest);
        MediaType mediaType = getMediaType(swaggerHubRequest);
        Request requestBuilder = buildGetRequest(httpUrl, mediaType);

        try (Response response = CLIENT.newCall(requestBuilder).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;
            if (responseBody == null) {
                throw new GradleException(DOWNLOAD_FAILED_ERROR + "Response body is empty");
            } else if (!response.isSuccessful()) {
                throw new GradleException(DOWNLOAD_FAILED_ERROR + responseBody);
            } else {
                return responseBody;
            }
        } catch (IOException e) {
            throw new GradleException(DOWNLOAD_FAILED_ERROR, e);
        }
    }

    public void saveDefinition(SwaggerHubRequest swaggerHubRequest) throws GradleException {
        HttpUrl httpUrl = getUploadUrl(swaggerHubRequest);
        MediaType mediaType = getMediaType(swaggerHubRequest);
        Request httpRequest = buildPostRequest(httpUrl, mediaType, swaggerHubRequest.getSwagger());

        try (Response response = CLIENT.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;

            if (responseBody == null) {
                throw new GradleException(UPLOAD_FAILED_ERROR + "Response body is empty");
            } else if (!response.isSuccessful()) {
                throw new GradleException(UPLOAD_FAILED_ERROR + responseBody);
            }
        } catch (IOException e) {
            throw new GradleException(UPLOAD_FAILED_ERROR, e);
        }
    }

    public void saveDefinitionPUT(SwaggerHubRequest swaggerHubRequest) throws GradleException {
        HttpUrl httpUrl = getDefaultVersionUrl(swaggerHubRequest);
        Request httpRequest = buildPutRequest(httpUrl, swaggerHubRequest.getVersion());

        try (Response response = CLIENT.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;

            if (responseBody == null) {
                throw new GradleException(UPLOAD_FAILED_ERROR + "Response body is empty");
            } else if (!response.isSuccessful()) {
                throw new GradleException(UPLOAD_FAILED_ERROR + responseBody);
            }
        } catch (IOException e) {
            throw new GradleException(UPLOAD_FAILED_ERROR, e);
        }
    }

    private Request buildGetRequest(HttpUrl httpUrl, MediaType mediaType) {
        Request.Builder requestBuilder =
                new Request.Builder()
                        .url(httpUrl)
                        .addHeader("Accept", mediaType.toString())
                        .addHeader("User-Agent", "swaggerhub-gradle-plugin");
        if (token != null) {
            requestBuilder.addHeader("Authorization", token);
        }
        return requestBuilder.build();
    }

    private Request buildPostRequest(HttpUrl httpUrl, MediaType mediaType, String content) {
        return new Request.Builder()
                .url(httpUrl)
                .addHeader("Content-Type", mediaType.toString())
                .addHeader("Authorization", token)
                .addHeader("User-Agent", "swaggerhub-gradle-plugin")
                .post(RequestBody.create(content, mediaType))
                .build();
    }

    private Request buildPutRequest(HttpUrl httpUrl, String content) {
        String jsonBody = "{\"version\": \"" + content + "\"}";

        return new Request.Builder()
                .url(httpUrl)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Authorization", token)
                .addHeader("User-Agent", "swaggerhub-gradle-plugin")
                .put(
                        RequestBody.create(
                                jsonBody, MediaType.parse("application/json; charset=utf-8")))
                .build();
    }

    private HttpUrl getDownloadUrl(SwaggerHubRequest swaggerHubRequest) {
        return getBaseUrl(swaggerHubRequest.getOwner(), swaggerHubRequest.getApi())
                .addEncodedPathSegment(swaggerHubRequest.getVersion())
                .addQueryParameter("resolved", String.valueOf(swaggerHubRequest.getResolved()))
                .build();
    }

    private HttpUrl getUploadUrl(SwaggerHubRequest swaggerHubRequest) {
        return getBaseUrl(swaggerHubRequest.getOwner(), swaggerHubRequest.getApi())
                .addEncodedQueryParameter("version", swaggerHubRequest.getVersion())
                .addEncodedQueryParameter(
                        "isPrivate", Boolean.toString(swaggerHubRequest.getIsPrivate()))
                .addEncodedQueryParameter("oas", swaggerHubRequest.getOas())
                .build();
    }

    private HttpUrl getDefaultVersionUrl(SwaggerHubRequest swaggerHubRequest) {
        return getBaseUrl(swaggerHubRequest.getOwner(), swaggerHubRequest.getApi())
                .addEncodedPathSegment("settings")
                .addEncodedPathSegment("default")
                .build();
    }

    private HttpUrl.Builder getBaseUrl(String owner, String api) {
        return new HttpUrl.Builder()
                .scheme(protocol)
                .host(host)
                .port(port)
                .addPathSegment(onPremise ? onPremiseAPISuffix : "")
                .addPathSegment(APIS)
                .addEncodedPathSegment(owner)
                .addEncodedPathSegment(api);
    }

    private MediaType getMediaType(SwaggerHubRequest swaggerHubRequest) {
        String headerFormat = "application/%s; charset=utf-8";
        MediaType mediaType =
                MediaType.parse(String.format(headerFormat, swaggerHubRequest.getFormat()));
        if (mediaType == null) {
            mediaType = MediaType.parse(String.format(headerFormat, "json"));
        }
        return mediaType;
    }
}

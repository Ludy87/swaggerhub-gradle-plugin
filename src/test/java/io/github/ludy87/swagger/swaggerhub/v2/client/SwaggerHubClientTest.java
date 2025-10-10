/*
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class SwaggerHubClientTest {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void createOnPremiseDefaultsNullValues() {
        SwaggerHubClient client =
                SwaggerHubClient.createOnPremise(
                        "api.swaggerhub.com", 443, "https", "token", null, null);

        assertFalse(client.getOnPremise());
        assertEquals("v1", client.getOnPremiseAPISuffix());
    }

    @Test
    public void getDefinitionUsesResolvedQueryAndHeaders() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("{\"status\":\"ok\"}"));

        SwaggerHubClient client =
                SwaggerHubClient.createOnPremise(
                        mockWebServer.getHostName(),
                        mockWebServer.getPort(),
                        "http",
                        null,
                        true,
                        "custom");

        SwaggerHubRequest request =
                SwaggerHubRequest.builder()
                        .owner("example")
                        .api("petstore")
                        .version("1.0.0")
                        .format("json")
                        .resolved(true)
                        .build();

        String response = client.getDefinition(request);
        assertThat(response, is("{\"status\":\"ok\"}"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(
                "/custom/apis/example/petstore/1.0.0",
                recordedRequest.getRequestUrl().encodedPath());
        assertEquals("true", recordedRequest.getRequestUrl().queryParameter("resolved"));
        assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Accept"));
        assertEquals("swaggerhub-gradle-plugin", recordedRequest.getHeader("User-Agent"));
    }

    @Test
    public void saveDefinitionSendsExpectedPayload() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(201)
                        .addHeader("Content-Type", "application/json")
                        .setBody("created"));

        SwaggerHubClient client =
                SwaggerHubClient.createOnPremise(
                        mockWebServer.getHostName(),
                        mockWebServer.getPort(),
                        "http",
                        "token123",
                        true,
                        "v2");

        SwaggerHubRequest request =
                SwaggerHubRequest.builder()
                        .owner("example")
                        .api("petstore")
                        .version("1.0.0")
                        .format("yaml")
                        .swagger("openapi: 3.0.0")
                        .oas("3.0.0")
                        .isPrivate(true)
                        .build();

        client.saveDefinition(request);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals(
                "/v2/apis/example/petstore", recordedRequest.getRequestUrl().encodedPath());
        assertEquals("1.0.0", recordedRequest.getRequestUrl().queryParameter("version"));
        assertEquals("true", recordedRequest.getRequestUrl().queryParameter("isPrivate"));
        assertEquals("3.0.0", recordedRequest.getRequestUrl().queryParameter("oas"));
        assertEquals("application/yaml; charset=utf-8", recordedRequest.getHeader("Content-Type"));
        assertEquals("token123", recordedRequest.getHeader("Authorization"));
        assertEquals("swaggerhub-gradle-plugin", recordedRequest.getHeader("User-Agent"));
        assertEquals("openapi: 3.0.0", recordedRequest.getBody().readUtf8());
    }

    @Test
    public void saveDefinitionPutTargetsDefaultVersionEndpoint() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(204)
                        .addHeader("Content-Type", "application/json")
                        .setBody("no content"));

        SwaggerHubClient client =
                SwaggerHubClient.createOnPremise(
                        mockWebServer.getHostName(),
                        mockWebServer.getPort(),
                        "http",
                        "token123",
                        false,
                        null);

        SwaggerHubRequest request =
                SwaggerHubRequest.builder()
                        .owner("example")
                        .api("petstore")
                        .version("2.0.0")
                        .build();

        client.saveDefinitionPUT(request);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("PUT", recordedRequest.getMethod());
        assertEquals(
                "/apis/example/petstore/settings/default",
                recordedRequest.getRequestUrl().encodedPath());
        assertEquals("{\"version\": \"2.0.0\"}", recordedRequest.getBody().readUtf8());
        assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Content-Type"));
        assertEquals("token123", recordedRequest.getHeader("Authorization"));
        assertEquals("swaggerhub-gradle-plugin", recordedRequest.getHeader("User-Agent"));
    }
}


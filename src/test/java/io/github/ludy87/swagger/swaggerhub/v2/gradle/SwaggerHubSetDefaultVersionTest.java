/* Copyright 2025 Ludy87
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
package io.github.ludy87.swagger.swaggerhub.v2.gradle;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;

import io.github.ludy87.swagger.swaggerhub.v2.client.SwaggerHubRequest;

@SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:JavadocVariable"})
public class SwaggerHubSetDefaultVersionTest {

    /** Temporary directory for the generated Gradle project. */
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();

    /** Name of the Gradle task under test. */
    private static final String SETDEFAULTVERSION_TASK = "swaggerhubSetDefaultVersion";

    private WireMockServer wireMockServer;
    private File buildFile;

    private final String api = "TestAPI";
    private final String owner = "testUser";
    private final String version = "1.1.0";
    private final String host = "localhost";
    private final String serverPort = "8089";
    private final String token = "dUmMyTokEn.1234abc";

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
    }

    @After
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testSetDefaultVersionPUT() throws IOException, URISyntaxException {
        SwaggerHubRequest request =
                SwaggerHubRequest.builder().api(api).owner(owner).version(version).build();

        setupServerMockingPUT(request, serverPort, token);

        assertEquals(SUCCESS, runBuild(request));
    }

    private TaskOutcome runBuild(final SwaggerHubRequest request) throws IOException {
        createBuildFile(request);

        BuildResult result =
                GradleRunner.create()
                        .withPluginClasspath()
                        .withProjectDir(testProjectDir.getRoot())
                        .withArguments(SETDEFAULTVERSION_TASK, "--stacktrace")
                        .build();

        return result.task(":" + SETDEFAULTVERSION_TASK).getOutcome();
    }

    private void createBuildFile(final SwaggerHubRequest request) throws IOException {
        String buildFileContent =
                "plugins { id 'io.github.ludy87.swagger.swaggerhub.v2' }\n"
                        + SETDEFAULTVERSION_TASK
                        + " {\n"
                        + "    host '"
                        + host
                        + "'\n"
                        + "    port "
                        + serverPort
                        + "\n"
                        + "    protocol 'http'\n"
                        + "    api '"
                        + request.getApi()
                        + "'\n"
                        + "    owner '"
                        + request.getOwner()
                        + "'\n"
                        + "    version '"
                        + request.getVersion()
                        + "'\n"
                        + "    token '"
                        + token
                        + "'\n"
                        + "}";

        Files.write(buildFile.toPath(), buildFileContent.getBytes());
    }

    private void setupServerMockingPUT(
            final SwaggerHubRequest request, final String portValue, final String authToken) {
        startMockServer(Integer.parseInt(portValue));
        String jsonBody = "{\"version\": \"" + request.getVersion() + "\"}";

        UrlPathPattern url =
                urlPathEqualTo(
                        "/apis/"
                                + request.getOwner()
                                + "/"
                                + request.getApi()
                                + "/settings/default");

        stubFor(
                put(url).withHeader(
                                "Content-Type",
                                equalToIgnoreCase("application/json; charset=UTF-8"))
                        .withHeader("Authorization", equalTo(authToken))
                        .withHeader("User-Agent", equalTo("swaggerhub-gradle-plugin"))
                        .withRequestBody(equalTo(jsonBody))
                        .willReturn(noContent()));
    }

    private void startMockServer(final int httpPort) {
        wireMockServer = new WireMockServer(httpPort);
        wireMockServer.start();
        configureFor(host, wireMockServer.port());
    }
}

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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
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
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;

import io.github.ludy87.swagger.swaggerhub.v2.client.SwaggerHubRequest;

public class SwaggerHubSetDefaultVersionTest {

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private static final String SETDEFAULTVERSION_TASK = "swaggerhubSetDefaultVersion";
    private WireMockServer wireMockServer;
    private File buildFile;

    private final String api = "TestAPI";
    private final String owner = "testUser";
    private final String version = "1.1.0";
    private final String host = "localhost";
    private final String port = "8089";
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

        setupServerMockingPUT(request, port, token);

        assertEquals(SUCCESS, runBuild(request));
    }

    private TaskOutcome runBuild(SwaggerHubRequest request) throws IOException {
        createBuildFile(request);

        BuildResult result =
                GradleRunner.create()
                        .withPluginClasspath()
                        .withProjectDir(testProjectDir.getRoot())
                        .withArguments(SETDEFAULTVERSION_TASK, "--stacktrace")
                        .build();

        return result.task(":" + SETDEFAULTVERSION_TASK).getOutcome();
    }

    private void createBuildFile(SwaggerHubRequest request) throws IOException {
        String buildFileContent =
                "plugins { id 'io.github.ludy87.swagger.swaggerhub.v2' }\n"
                        + SETDEFAULTVERSION_TASK
                        + " {\n"
                        + "    host '"
                        + host
                        + "'\n"
                        + "    port "
                        + port
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

    private void setupServerMockingPUT(SwaggerHubRequest request, String port, String token) {
        startMockServer(Integer.parseInt(port));
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
                        .withHeader("Authorization", equalTo(token))
                        .withHeader("User-Agent", equalTo("swaggerhub-gradle-plugin"))
                        .withRequestBody(equalTo(jsonBody))
                        .willReturn(noContent()));
    }

    private void startMockServer(int port) {
        wireMockServer = new WireMockServer(port);
        wireMockServer.start();
        WireMock.configureFor(host, wireMockServer.port());
    }
}

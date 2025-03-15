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
package io.swagger.swaggerhub.v2.gradle;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.StringUtils;
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

import io.swagger.swaggerhub.v2.client.SwaggerHubRequest;

public class SwaggerHubUploadTest {

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private static final String UPLOAD_TASK = "swaggerhubUpload";
    private WireMockServer wireMockServer;
    private File buildFile;
    private Path inputFile;
    private String testInputAPI = "TestAPI.json";

    private final String api = "TestAPI";
    private final String owner = "testUser";
    private final String version = "1.1.0";
    private final String host = "localhost";
    private final String port = "8089";
    private final String token = "dUmMyTokEn.1234abc";
    private String swagger;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
    }

    @After
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testUploadOnPremise() throws IOException, URISyntaxException {
        copyInputFile(testInputAPI, testProjectDir.getRoot());
        inputFile = getInputFilePath(testInputAPI);
        swagger = Files.readString(inputFile, StandardCharsets.UTF_8);

        SwaggerHubRequest request =
                SwaggerHubRequest.builder()
                        .api(api)
                        .owner(owner)
                        .version(version)
                        .swagger(swagger)
                        .build();

        setupServerMocking(request, port, token);
        assertEquals(SUCCESS, runBuild(request));
    }

    @Test
    public void testUpload() throws IOException, URISyntaxException {
        copyInputFile(testInputAPI, testProjectDir.getRoot());
        inputFile = getInputFilePath(testInputAPI);
        swagger = Files.readString(inputFile, StandardCharsets.UTF_8);

        SwaggerHubRequest request =
                SwaggerHubRequest.builder()
                        .api(api)
                        .owner(owner)
                        .version(version)
                        .swagger(swagger)
                        .build();

        setupServerMocking(request, port, token);
        assertEquals(SUCCESS, runBuild(request));
    }

    @Test
    public void testUploadPrivate() throws IOException, URISyntaxException {
        copyInputFile(testInputAPI, testProjectDir.getRoot());
        inputFile = getInputFilePath(testInputAPI);
        swagger = Files.readString(inputFile, StandardCharsets.UTF_8);

        SwaggerHubRequest request =
                SwaggerHubRequest.builder()
                        .api(api)
                        .owner(owner)
                        .version(version)
                        .swagger(swagger)
                        .isPrivate(true)
                        .build();

        setupServerMocking(request, port, token);
        assertEquals(SUCCESS, runBuild(request));
    }

    @Test
    public void testUploadPublic() throws IOException, URISyntaxException {
        copyInputFile(testInputAPI, testProjectDir.getRoot());
        inputFile = getInputFilePath(testInputAPI);
        swagger = Files.readString(inputFile, StandardCharsets.UTF_8);

        SwaggerHubRequest request =
                SwaggerHubRequest.builder()
                        .api(api)
                        .owner(owner)
                        .version(version)
                        .swagger(swagger)
                        .build();

        setupServerMocking(request, port, token);
        assertEquals(SUCCESS, runBuild(request));
    }

    @Test
    public void testUploadYaml() throws Exception {
        testInputAPI = "TestAPI.yaml";
        copyInputFile(testInputAPI, testProjectDir.getRoot());
        inputFile = getInputFilePath(testInputAPI);
        swagger = Files.readString(inputFile, StandardCharsets.UTF_8);

        SwaggerHubRequest request =
                SwaggerHubRequest.builder()
                        .api(api)
                        .owner(owner)
                        .version(version)
                        .format("yaml")
                        .swagger(swagger)
                        .build();

        setupServerMocking(request, port, token);
        assertEquals(SUCCESS, runBuild(request));
    }

    private TaskOutcome runBuild(SwaggerHubRequest request) throws IOException {
        createBuildFile(request);

        BuildResult result =
                GradleRunner.create()
                        .withPluginClasspath()
                        .withProjectDir(testProjectDir.getRoot())
                        .withArguments(UPLOAD_TASK, "--stacktrace")
                        .build();

        return result.task(":" + UPLOAD_TASK).getOutcome();
    }

    private void createBuildFile(SwaggerHubRequest request) throws IOException {
        // Convert Windows path to use slashes for Gradle config
        String filePath = inputFile.toString().replace("\\", "/");

        String buildFileContent =
                "plugins { id 'io.swagger.swaggerhub.v2' }\n"
                        + UPLOAD_TASK
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
                        + getFormatSetting(request.getFormat())
                        + getIsPrivateSetting(Boolean.TRUE.equals(request.getIsPrivate()))
                        + "    inputFile '"
                        + filePath
                        + "'\n"
                        + "    token '"
                        + token
                        + "'\n"
                        + "}";

        Files.write(buildFile.toPath(), buildFileContent.getBytes());
    }

    private String getIsPrivateSetting(Boolean isPrivate) {
        // false is default, so only include if set to true
        return isPrivate ? String.format("    isPrivate %s\n", isPrivate) : "";
    }

    private String getFormatSetting(String format) {
        // json is default, so only include if set to yaml
        return StringUtils.isNotBlank(format) && "yaml".equals(format)
                ? String.format("    format '%s'\n", format)
                : "";
    }

    private void copyInputFile(String originalFile, File outputDir)
            throws IOException, URISyntaxException {
        Path copied = Paths.get(outputDir.getPath(), originalFile);
        Path originalFilePath =
                Paths.get(ClassLoader.getSystemClassLoader().getResource(originalFile).toURI());
        Files.copy(originalFilePath, copied, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path getInputFilePath(String filename) {
        return Paths.get(testProjectDir.getRoot().toString(), filename);
    }

    private UrlPathPattern setupServerMocking(
            SwaggerHubRequest request, String port, String token) {
        String api = request.getApi();
        String owner = request.getOwner();
        String version = request.getVersion();
        String format = request.getFormat();
        String isPrivate = Boolean.toString(Boolean.TRUE.equals(request.getIsPrivate()));

        startMockServer(Integer.parseInt(port));

        UrlPathPattern url = urlPathEqualTo("/apis/" + owner + "/" + api);

        stubFor(
                post(url)
                        .withQueryParam("version", equalTo(version))
                        .withQueryParam("isPrivate", equalTo(isPrivate))
                        .withHeader(
                                "Content-Type",
                                equalToIgnoreCase(
                                        String.format(
                                                "application/%s; charset=UTF-8",
                                                format != null ? format : "json")))
                        .withHeader("Authorization", equalTo(token))
                        .withHeader("User-Agent", equalTo("swaggerhub-gradle-plugin"))
                        .withRequestBody(equalTo(request.getSwagger()))
                        .willReturn(created()));

        return url;
    }

    private void startMockServer(int port) {
        wireMockServer = new WireMockServer(options().port(port));
        wireMockServer.start();
        WireMock.configureFor(host, wireMockServer.port());
    }
}

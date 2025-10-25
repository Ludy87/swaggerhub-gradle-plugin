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
package io.github.ludy87.swagger.swaggerhub.v2.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;

import io.github.ludy87.swagger.swaggerhub.v2.client.SwaggerHubClient;
import io.github.ludy87.swagger.swaggerhub.v2.client.SwaggerHubRequest;

import lombok.Getter;
import lombok.Setter;

/** Downloads API definitions from SwaggerHub. */
@Getter
@Setter
public class DownloadTask extends DefaultTask {
    /** Logger instance for the task. */
    private static final Logger LOGGER = Logging.getLogger(DownloadTask.class);

    /** Default HTTPS port used by SwaggerHub. */
    private static final int DEFAULT_PORT = 443;

    /** Owner of the API. */
    @Input private String owner;

    /** API identifier. */
    @Input private String api;

    /** Version to download. */
    @Input private String version;

    /** Optional authentication token. */
    @Input @Optional private String token;

    /** File path for the downloaded definition. */
    @Input private String outputFile;

    /** Desired response format. */
    @Input @Optional private String format = "json";

    /** SwaggerHub host name. */
    @Input @Optional private String host = "api.swaggerhub.com";

    /** SwaggerHub port. */
    @Input @Optional private Integer port = DEFAULT_PORT;

    /** Communication protocol. */
    @Input @Optional private String protocol = "https";

    /** Indicates whether a resolved definition should be retrieved. */
    @Input @Optional private Boolean resolved = false;

    /** Signals if an on-premise instance is used. */
    @Input @Optional private Boolean onPremise = false;

    /** API suffix to use for on-premise SwaggerHub installations (e.g., "v1"). */
    @Input @Optional private String onPremiseAPISuffix = "v1";

    /** SwaggerHub client used for the download. */
    @Internal private SwaggerHubClient swaggerHubClient;

    /**
     * Downloads the API definition from SwaggerHub.
     *
     * @throws GradleException if an error occurs during the download process
     */
    @TaskAction
    public void downloadDefinition() throws GradleException {
        swaggerHubClient = SwaggerHubClient.create(host, port, protocol, token);

        LOGGER.info(
                "Downloading from {}: api={}, owner={}, version={}, format={}, "
                        + "resolved={}, outputFile={}, onPremise={}, onPremiseAPISuffix={}",
                host,
                api,
                owner,
                version,
                format,
                resolved,
                outputFile,
                onPremise,
                onPremiseAPISuffix);

        SwaggerHubRequest swaggerHubRequest =
                SwaggerHubRequest.builder()
                        .api(api)
                        .owner(owner)
                        .version(version)
                        .format(format)
                        .resolved(resolved)
                        .build();

        try {
            String swaggerJson = swaggerHubClient.getDefinition(swaggerHubRequest);
            File file = new File(outputFile);

            setUpOutputDir(file);
            Files.write(Paths.get(outputFile), swaggerJson.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | GradleException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }

    /**
     * Sets up the output directory for the downloaded file.
     *
     * @param file the file for which the parent directory should be created
     * @throws IOException if an error occurs while creating the directories
     */
    private void setUpOutputDir(final File file) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            Files.createDirectories(file.getParentFile().toPath());
        }
    }
}

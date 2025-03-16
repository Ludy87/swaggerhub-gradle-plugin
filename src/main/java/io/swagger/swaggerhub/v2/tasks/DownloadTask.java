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
package io.swagger.swaggerhub.v2.tasks;

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

import io.swagger.swaggerhub.v2.client.SwaggerHubClient;
import io.swagger.swaggerhub.v2.client.SwaggerHubRequest;

import lombok.Getter;
import lombok.Setter;

/** Downloads API definition from SwaggerHub */
@Getter
@Setter
public class DownloadTask extends DefaultTask {
    private static final Logger LOGGER = Logging.getLogger(DownloadTask.class);
    @Input private String owner;
    @Input private String api;
    @Input private String version;
    @Input @Optional private String token;
    @Input private String outputFile;
    @Input @Optional private String format = "json";
    @Input @Optional private String host = "api.swaggerhub.com";
    @Input @Optional private Integer port = 443;
    @Input @Optional private String protocol = "https";
    @Input @Optional private Boolean resolved = false;
    @Input @Optional private Boolean onPremise = false;
    @Input @Optional private String onPremiseAPISuffix = "v1";

    @Internal private SwaggerHubClient swaggerHubClient;

    @TaskAction
    public void downloadDefinition() throws GradleException {
        swaggerHubClient = SwaggerHubClient.create(host, port, protocol, token);

        LOGGER.info(
                "Downloading from {}: api: {}, owner: {}, version: {}, format: {}, resolved: {},"
                        + " outputFile: {}, onPremise: {}, onPremiseAPISuffix: {}",
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

    private void setUpOutputDir(File file) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            Files.createDirectories(file.getParentFile().toPath());
        }
    }
}

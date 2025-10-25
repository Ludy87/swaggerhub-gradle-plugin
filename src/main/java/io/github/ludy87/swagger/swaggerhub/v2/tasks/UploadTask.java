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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;

import io.github.ludy87.swagger.swaggerhub.v2.client.SwaggerHubClient;
import io.github.ludy87.swagger.swaggerhub.v2.client.SwaggerHubRequest;

import lombok.Getter;
import lombok.Setter;

/** Uploads API definition to SwaggerHub. */
@Getter
@Setter
public class UploadTask extends DefaultTask {
    /** Logger instance for the task. */
    private static final Logger LOGGER = Logging.getLogger(UploadTask.class);

    /** Default HTTPS port used by SwaggerHub. */
    private static final int DEFAULT_PORT = 443;

    /** API owner. */
    @Input private String owner;

    /** API identifier. */
    @Input private String api;

    /** API version. */
    @Input private String version;

    /** Authentication token. */
    @Input private String token;

    /** Path to the API definition file. */
    @InputFile private String inputFile;

    /** Flag indicating whether the API is private. */
    @Input private Boolean isPrivate = false;

    /** SwaggerHub host name. */
    @Input @Optional private String host = "api.swaggerhub.com";

    /** SwaggerHub port. */
    @Input @Optional private Integer port = DEFAULT_PORT;

    /** Protocol used for requests. */
    @Input @Optional private String protocol = "https";

    /** Response format. */
    @Input @Optional private String format = "json";

    /** OAS version. */
    @Input @Optional private String oas = "2.0";

    /** Indicates that an on-premise instance is used. */
    @Input @Optional private Boolean onPremise = false;

    /** On-premise API suffix. */
    @Input @Optional private String onPremiseAPISuffix = "v1";

    /** SwaggerHub client used to perform the upload. */
    @Internal private SwaggerHubClient swaggerHubClient;

    /**
     * Uploads the API definition to SwaggerHub.
     *
     * @throws GradleException if there is an error during the upload process
     */
    @TaskAction
    public void uploadDefinition() throws GradleException {
        swaggerHubClient =
                SwaggerHubClient.createOnPremise(
                        host, port, protocol, token, onPremise, onPremiseAPISuffix);
        LOGGER.info(
                "Uploading to {}: api={}, owner={}, version={}, inputFile={}, format={}, "
                        + "isPrivate={}, oas={}, onPremise={}, onPremiseAPISuffix={}",
                host,
                api,
                owner,
                version,
                inputFile,
                format,
                isPrivate,
                oas,
                onPremise,
                onPremiseAPISuffix);

        try {
            String content =
                    new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8);

            SwaggerHubRequest swaggerHubRequest =
                    SwaggerHubRequest.builder()
                            .api(api)
                            .owner(owner)
                            .version(version)
                            .format(format)
                            .swagger(content)
                            .oas(oas)
                            .onPremise(onPremise)
                            .onPremiseAPISuffix(onPremiseAPISuffix)
                            .isPrivate(isPrivate)
                            .build();

            swaggerHubClient.saveDefinition(swaggerHubRequest);
        } catch (IOException | GradleException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }
}

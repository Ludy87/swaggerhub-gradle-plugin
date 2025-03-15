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
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import io.swagger.swaggerhub.v2.client.SwaggerHubClient;
import io.swagger.swaggerhub.v2.client.SwaggerHubRequest;

import lombok.extern.slf4j.Slf4j;

/** Downloads API definition from SwaggerHub */
@Slf4j
public class DownloadTask extends DefaultTask {
    private String owner;
    private String api;
    private String version;
    private String token;
    private String outputFile;
    private String format = "json";
    private String host = "api.swaggerhub.com";
    private Integer port = 443;
    private String protocol = "https";
    private Boolean resolved = false;
    private Boolean onPremise = false;
    private String onPremiseAPISuffix = "v1";

    @Input
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Input
    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    @Input
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Input
    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    @Input
    @Optional
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Input
    @Optional
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Input
    @Optional
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Input
    @Optional
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Input
    @Optional
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Input
    @Optional
    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    @Input
    @Optional
    public Boolean getOnPremise() {
        return onPremise;
    }

    public void setOnPremise(Boolean onPremise) {
        this.onPremise = onPremise;
    }

    @Input
    @Optional
    public String getOnPremiseAPISuffix() {
        return onPremiseAPISuffix;
    }

    public void setOnPremiseAPISuffix(String onPremiseAPISuffix) {
        this.onPremiseAPISuffix = onPremiseAPISuffix;
    }

    @TaskAction
    public void downloadDefinition() throws GradleException {
        SwaggerHubClient swaggerHubClient =
                new SwaggerHubClient(host, port, protocol, token, onPremise, onPremiseAPISuffix);

        log.info(
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
                new SwaggerHubRequest.Builder(api, owner, version)
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

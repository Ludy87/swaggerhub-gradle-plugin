package io.swagger.swaggerhub.v2.tasks;

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

@Getter
@Setter
public class SetDefaultVersion extends DefaultTask {
    private static final Logger LOGGER = Logging.getLogger(SetDefaultVersion.class);
    @Input private String owner;
    @Input private String api;
    @Input private String version;
    @Input private String token;
    @Input @Optional private String host = "api.swaggerhub.com";
    @Input @Optional private Integer port = 443;
    @Input @Optional private String protocol = "https";
    @Input @Optional private Boolean onPremise = false;
    @Input @Optional private String onPremiseAPISuffix = "v1";

    @Internal private SwaggerHubClient swaggerHubClient;

    @TaskAction
    public void uploadDefinition() throws GradleException {
        swaggerHubClient =
                SwaggerHubClient.createOnPremise(
                        host, port, protocol, token, onPremise, onPremiseAPISuffix);

        LOGGER.info(
                "Setting default version to {}: api: {}, owner: {}, version: {}, onPremise: {},"
                        + " onPremiseAPISuffix: {}",
                host,
                api,
                owner,
                version,
                onPremise,
                onPremiseAPISuffix);

        try {
            SwaggerHubRequest swaggerHubRequest =
                    SwaggerHubRequest.builder()
                            .api(api)
                            .owner(owner)
                            .version(version)
                            .onPremise(onPremise)
                            .onPremiseAPISuffix(onPremiseAPISuffix)
                            .build();

            swaggerHubClient.saveDefinitionPUT(swaggerHubRequest);
        } catch (GradleException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }
}

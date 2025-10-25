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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Represents the payload sent to SwaggerHub operations. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SwaggerHubRequest {
    /** Name of the API. */
    private String api;

    /** Owner of the API. */
    private String owner;

    /** Version of the API. */
    private String version;

    /** API definition content (Swagger/OpenAPI specification). */
    private String swagger;

    /** API format such as OAS version. */
    private String oas;

    /** Desired output format. */
    @Builder.Default private String format = "json";

    /** Indicates whether the API is private. */
    @Builder.Default private Boolean isPrivate = false;

    /** Whether resolved API definition should be retrieved. */
    @Builder.Default private Boolean resolved = false;

    /** Indicates that the request targets an on-premise installation. */
    @Builder.Default private Boolean onPremise = false;

    /** API suffix for on-premise installations. */
    @Builder.Default private String onPremiseAPISuffix = "v1";
}

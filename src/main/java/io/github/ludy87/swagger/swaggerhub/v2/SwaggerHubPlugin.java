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
package io.github.ludy87.swagger.swaggerhub.v2;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.github.ludy87.swagger.swaggerhub.v2.tasks.DownloadTask;
import io.github.ludy87.swagger.swaggerhub.v2.tasks.SetDefaultVersion;
import io.github.ludy87.swagger.swaggerhub.v2.tasks.UploadTask;

public class SwaggerHubPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("swaggerhubDownload", DownloadTask.class);
        project.getTasks().register("swaggerhubUpload", UploadTask.class);
        project.getTasks().register("swaggerhubSetDefaultVersion", SetDefaultVersion.class);
    }
}

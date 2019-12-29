/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.erdi.gradle.webdriver

import com.github.erdi.gradle.webdriver.repository.DriverUrlsConfiguration
import org.gradle.api.Project
import org.gradle.api.resources.TextResource
import org.ysb33r.grolifant.api.AbstractDistributionInstaller

class DriverDistributionInstaller extends AbstractDistributionInstaller {

    private final TextResource repositoryResource
    private final DriverDownloadSpecification spec

    DriverDistributionInstaller(Project project, TextResource repositoryResource, File downloadRoot, DriverDownloadSpecification spec) {
        super(spec.name, spec.version, "webdriver/$spec.name/$spec.version", project)
        this.repositoryResource = repositoryResource
        this.downloadRoot = downloadRoot
        this.spec = spec
    }

    @Override
    protected File getAndVerifyDistributionRoot(File distDir, String distributionDescription) {
        distDir
    }

    @Override
    URI uriFromVersion(String version) {
        new DriverUrlsConfiguration(repositoryResource.asFile()).uriFor(spec)
    }
}

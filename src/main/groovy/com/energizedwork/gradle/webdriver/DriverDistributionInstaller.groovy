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
package com.energizedwork.gradle.webdriver

import com.energizedwork.gradle.webdriver.repository.DriverUrlsConfiguration
import org.gradle.api.Project
import org.gradle.api.resources.TextResource
import org.ysb33r.grolifant.api.AbstractDistributionInstaller
import org.ysb33r.grolifant.api.OperatingSystem
import org.ysb33r.grolifant.api.OperatingSystem.Arch

class DriverDistributionInstaller extends AbstractDistributionInstaller {

    private final String driverName
    private final TextResource repositoryResource
    private final OperatingSystem os
    private final Arch arch

    DriverDistributionInstaller(Project project, String driverName, TextResource repositoryResource, File downloadRoot, String distributionVersion,
                                OperatingSystem os, Arch arch) {
        super(driverName, distributionVersion, "webdriver/$driverName/$distributionVersion", project)
        this.driverName = driverName
        this.repositoryResource = repositoryResource
        this.downloadRoot = downloadRoot
        this.os = os
        this.arch = arch
    }

    @Override
    protected File getAndVerifyDistributionRoot(File distDir, String distributionDescription) {
        distDir
    }

    @Override
    URI uriFromVersion(String version) {
        new DriverUrlsConfiguration(repositoryResource.asFile()).uriFor(driverName, version, os, arch)
    }
}

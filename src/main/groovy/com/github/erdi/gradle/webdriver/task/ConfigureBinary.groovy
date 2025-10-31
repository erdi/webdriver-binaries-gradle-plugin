/*
 * Copyright 2017 the original author or authors.
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
package com.github.erdi.gradle.webdriver.task

import com.github.erdi.gradle.webdriver.DriverBinaryAware
import com.github.erdi.gradle.webdriver.DriverDistributionInstaller
import com.github.erdi.gradle.webdriver.DriverDownloadSpecification
import com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata
import com.github.erdi.gradle.webdriver.repository.DriverUrlsConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant.api.core.OperatingSystem
import org.ysb33r.grolifant.api.core.OperatingSystem.Arch

import javax.inject.Inject

abstract class ConfigureBinary extends DefaultTask {

    private final String driverName

    protected final List<DriverBinaryAware> binaryAwares = []

    @Internal
    final WebDriverBinaryMetadata webDriverBinaryMetadata

    private final ObjectFactory objectFactory

    @Inject
    protected ConfigureBinary(ObjectFactory objectFactory, WebDriverBinaryMetadata webDriverBinaryMetadata, String driverName) {
        this.objectFactory = objectFactory
        this.webDriverBinaryMetadata = webDriverBinaryMetadata
        this.driverName = driverName
        onlyIf { versionConfigured }
    }

    @Internal
    abstract Property<TextResource> getDriverUrlsConfiguration()

    @Internal
    abstract DirectoryProperty getDownloadRoot()

    @Internal
    abstract Property<String> getVersion()

    @Internal
    abstract Property<Arch> getArchitecture()

    @Internal
    abstract Property<Boolean> getFallbackTo32Bit()

    void addBinaryAware(DriverBinaryAware aware) {
        binaryAwares << aware
    }

    @TaskAction
    void configure() {
        def versionAndUri = new DriverUrlsConfiguration(driverUrlsConfiguration.get().asFile()).versionAndUriFor(downloadSpec())
        def installer = new DriverDistributionInstaller(project, downloadRoot.asFile.get(), driverName, versionAndUri)
        def distributionRoot = installer.getDistributionRoot(versionAndUri.version).get()
        def osSpecificBinaryNames = operatingSystem.getExecutableNames(webDriverBinaryMetadata.binaryName)

        def binaryFile = objectFactory.fileTree().tap {
            from(distributionRoot)
            osSpecificBinaryNames.each {
                // file name is case sensitive and some are provided in upper case
                include(["**/${it}", "**/${it.toLowerCase()}"])
            }
        }.singleFile
        def binaryAbsolutePath = binaryFile.absolutePath
        binaryAwares*.setDriverBinaryPathAndVersion(binaryAbsolutePath, versionAndUri.version)
    }

    @Internal
    protected OperatingSystem getOperatingSystem() {
        OperatingSystem.current()
    }

    @Internal
    protected boolean isVersionConfigured() {
        version.present
    }

    private DriverDownloadSpecification downloadSpec() {
        DriverDownloadSpecification.builder()
            .name(driverName)
            .version(version.get())
            .arch(architecture.get())
            .os(operatingSystem)
            .fallbackTo32Bit(fallbackTo32Bit.get())
            .build()
    }

}

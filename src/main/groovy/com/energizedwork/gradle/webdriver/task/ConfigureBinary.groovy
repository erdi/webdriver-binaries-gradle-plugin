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
package com.energizedwork.gradle.webdriver.task

import com.energizedwork.gradle.webdriver.DriverBinaryAware
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant.api.AbstractDistributionInstaller
import org.ysb33r.grolifant.api.OperatingSystem
import org.ysb33r.grolifant.api.OperatingSystem.Arch

abstract class ConfigureBinary extends DefaultTask {

    private final Property<TextResource> driverUrlsConfigurationProperty = project.objects.property(TextResource)
    private final Property<File> downloadRootProperty = project.objects.property(File)
    private final Property<String> versionProperty = project.objects.property(String)
    private final Property<Arch> architectureProperty = project.objects.property(Arch)

    protected final List<DriverBinaryAware> binaryAwares = []

    protected ConfigureBinary() {
        onlyIf { versionConfigured }
    }

    void setDownloadRoot(Provider<File> downloadRootProvider) {
        downloadRootProperty.set(downloadRootProvider)
    }

    void setDownloadRoot(File downloadRoot) {
        downloadRoot.set(downloadRoot)
    }

    @Internal
    File getDownloadRoot() {
        downloadRootProperty.orNull
    }

    void setVersion(Provider<String> versionProvider) {
        this.versionProperty.set(versionProvider)
    }

    void setVersion(String version) {
        this.versionProperty.set(version)
    }

    @Internal
    String getVersion() {
        versionProperty.get()
    }

    void setArchitecture(Provider<Arch> architectureProvider) {
        this.architectureProperty.set(architectureProvider)
    }

    void setArchitecture(Arch architecture) {
        this.architectureProperty.set(architecture)
    }

    @Internal
    Arch getArchitecture() {
        architectureProperty.get()
    }

    void setDriverUrlsConfiguration(Provider<TextResource> driverUrlsConfiguration) {
        this.driverUrlsConfigurationProperty.set(driverUrlsConfiguration)
    }

    void setDriverUrlsConfiguration(TextResource driverUrlsConfiguration) {
        this.driverUrlsConfigurationProperty.set(driverUrlsConfiguration)
    }

    @Internal
    TextResource getDriverUrlsConfiguration() {
        driverUrlsConfigurationProperty.get()
    }

    void addBinaryAware(DriverBinaryAware aware) {
        binaryAwares << aware
    }

    @TaskAction
    void configure() {
        def installer = distributionInstaller()
        def binaryFile = new File(installer.distributionRoot, operatingSystem.getExecutableName(binaryName))
        def binaryAbsolutePath = binaryFile.absolutePath
        binaryAwares*.driverBinaryPath = binaryAbsolutePath
    }

    @Internal
    protected abstract String getBinaryName()

    protected abstract AbstractDistributionInstaller distributionInstaller()

    @Internal
    protected OperatingSystem getOperatingSystem() {
        OperatingSystem.current()
    }

    @Internal
    protected boolean isVersionConfigured() {
        versionProperty.present
    }

}

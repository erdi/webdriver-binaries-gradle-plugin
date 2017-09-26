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
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.ysb33r.gradle.olifant.AbstractDistributionInstaller
import org.ysb33r.gradle.olifant.OperatingSystem

abstract class ConfigureBinary extends DefaultTask {

    private final PropertyState<File> downloadRoot = project.property(File)
    private final PropertyState<String> version = project.property(String)

    protected final List<DriverBinaryAware> binaryAwares = []

    protected ConfigureBinary() {
        onlyIf { versionConfigured }
    }

    void setDownloadRoot(Provider<File> downloadRootProvider) {
        downloadRoot.set(downloadRootProvider)
    }

    void setDownloadRoot(File downloadRoot) {
        downloadRoot.set(downloadRoot)
    }

    @Internal
    File getDownloadRoot() {
        downloadRoot.orNull
    }

    void setVersion(Provider<String> versionProvider) {
        this.version.set(versionProvider)
    }

    void setVersion(String version) {
        this.version.set(version)
    }

    @Internal
    String getVersion() {
        version.get()
    }

    void addBinaryAware(DriverBinaryAware aware) {
        binaryAwares << aware
    }

    @TaskAction
    void configure() {
        def installer = distributionInstaller()
        def os = OperatingSystem.current()
        def binaryFile = new File(installer.distributionRoot, os.getExecutableName(binaryName))
        def binaryAbsolutePath = binaryFile.absolutePath
        binaryAwares*.driverBinaryPath = binaryAbsolutePath
    }

    @Internal
    protected abstract String getBinaryName()

    protected abstract AbstractDistributionInstaller distributionInstaller()

    @Internal
    protected boolean isVersionConfigured() {
        version.present
    }
}

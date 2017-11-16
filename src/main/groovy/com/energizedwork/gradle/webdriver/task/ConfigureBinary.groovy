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
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant.api.AbstractDistributionInstaller
import org.ysb33r.grolifant.api.OperatingSystem

abstract class ConfigureBinary extends ConventionTask {

    @Internal
    File downloadRoot

    @Internal
    String version

    protected final List<DriverBinaryAware> binaryAwares = []

    protected ConfigureBinary() {
        onlyIf { versionConfigured }
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
        getVersion()
    }
}

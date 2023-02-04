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
import com.github.erdi.gradle.webdriver.task.CopyIntoDirectory
import com.github.erdi.gradle.webdriver.task.ResolveDriverBinary
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.ysb33r.grolifant.api.core.OperatingSystem

import javax.inject.Inject
import java.util.regex.Pattern

@SuppressWarnings(['AbstractClassWithPublicConstructor'])
abstract class DriverConfiguration {

    private final TaskContainer tasks
    protected final ProjectLayout projectLayout

    final Provider<Directory> driverBinaryPropertiesDirectory

    @Inject
    DriverConfiguration(TaskContainer tasks, ProjectLayout projectLayout, WebDriverBinaryMetadata webDriverBinaryMetadata) {
        this.tasks = tasks
        this.projectLayout = projectLayout

        this.architectureProperty.convention(OperatingSystem.current().arch)

        def resolveBinary = registerResolveDriverBinary(webDriverBinaryMetadata)

        def writeClasspathDirectory = registerWritePropertiesClasspathDir(webDriverBinaryMetadata)
        writeClasspathDirectory.configure {
            files.from(resolveBinary.flatMap { it.driverBinaryProperties })
        }

        driverBinaryPropertiesDirectory = versionProperty.flatMap {
            writeClasspathDirectory.flatMap { it.outputDirectory }
        }
    }

    abstract DirectoryProperty getDownloadRoot()
    abstract Property<TextResource> getDriverUrlsConfiguration()
    abstract Property<Pattern> getVersionProperty()
    abstract Property<OperatingSystem.Arch> getArchitectureProperty()
    abstract Property<Boolean> getFallbackTo32Bit()

    void setVersion(String version) {
        this.version = ~Pattern.quote(version)
    }

    void setVersion(Pattern version) {
        this.versionProperty.set(version)
    }

    void setArchitecture(String architecture) {
        def supportedArchitectures = DriverUrlsConfiguration.BITS.keySet()*.toString()
        if (!(architecture in supportedArchitectures)) {
            def quotedArchitectures = supportedArchitectures.collect { "'${it}'" }
            def supportedArchitecturesString = [quotedArchitectures[0..-2].join(', '), quotedArchitectures.last()].join(' and ')
            throw new IllegalArgumentException("Unsupported architecture value '$architecture'. Supported values are ${supportedArchitecturesString}.")
        }
        this.architecture = OperatingSystem.Arch.valueOf(architecture)
    }

    void setArchitecture(OperatingSystem.Arch architecture) {
        this.architectureProperty.set(architecture)
    }

    private TaskProvider<ResolveDriverBinary> registerResolveDriverBinary(WebDriverBinaryMetadata webDriverBinaryMetadata) {
        def name = "resolve${webDriverBinaryMetadata.driverName.capitalize()}Binary"
        tasks.register(name, ResolveDriverBinary, webDriverBinaryMetadata).tap {
            it.configure {
                downloadRoot.convention(this.downloadRoot)
                driverUrlsConfiguration.convention(this.driverUrlsConfiguration)
                version.convention(this.versionProperty)
                architecture.convention(this.architectureProperty)
                fallbackTo32Bit.convention(this.fallbackTo32Bit)
            }
        }
    }

    private TaskProvider<CopyIntoDirectory> registerWritePropertiesClasspathDir(WebDriverBinaryMetadata webDriverBinaryMetadata) {
        def name = "write${webDriverBinaryMetadata.driverName.capitalize()}PropertiesClasspathDirectory"
        tasks.register(name, CopyIntoDirectory) {
            outputDirectory.set(projectLayout.buildDirectory.dir(it.name))

            description = "Writes a directory containing the file with properties of the downloaded ${webDriverBinaryMetadata.driverName} binary."
        }
    }

}

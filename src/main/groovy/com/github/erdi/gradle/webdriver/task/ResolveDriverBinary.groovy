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

import com.github.erdi.gradle.webdriver.DriverDistributionInstaller
import com.github.erdi.gradle.webdriver.DriverDownloadSpecification
import com.github.erdi.gradle.webdriver.WebDriverBinariesPlugin
import com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata
import com.github.erdi.gradle.webdriver.repository.DriverUrlsConfiguration
import com.github.erdi.gradle.webdriver.repository.VersionAndUri
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import org.ysb33r.grolifant.api.core.OperatingSystem
import org.ysb33r.grolifant.api.core.OperatingSystem.Arch

import javax.inject.Inject
import java.util.regex.Pattern

@SuppressWarnings('AbstractClassWithPublicConstructor')
@DisableCachingByDefault(because = 'This is an I/O heavy task which would not benefit from caching')
abstract class ResolveDriverBinary extends DefaultTask {

    public static final String PROPERTY_FILE_NAME_PREFIX = 'com.github.erdi.gradle.webdriver'

    private final WebDriverBinaryMetadata webDriverBinaryMetadata
    private final FileSystemOperations fileSystemOperations

    @Inject
    ResolveDriverBinary(
        FileSystemOperations fileSystemOperations, ProjectLayout projectLayout, WebDriverBinaryMetadata webDriverBinaryMetadata
    ) {
        this.webDriverBinaryMetadata = webDriverBinaryMetadata
        this.fileSystemOperations = fileSystemOperations

        this.group = WebDriverBinariesPlugin.TASK_GROUP
        this.description = "Resolves a ${webDriverBinaryMetadata.driverName} binary from the configured cache location after downloading it if necessary. " +
            'Writes out a properties file with the system property name, path and version of the binary.'

        driverBinary.convention(projectLayout.buildDirectory.file("${name}/${binaryFileName}"))
        driverBinaryProperties.convention(projectLayout.buildDirectory.file(
            "${name}/" + PROPERTY_FILE_NAME_PREFIX + ".${webDriverBinaryMetadata.driverName}.properties")
        )
    }

    @Nested
    abstract Property<TextResource> getDriverUrlsConfiguration()

    @InputDirectory
    abstract DirectoryProperty getDownloadRoot()

    @Input
    abstract Property<Pattern> getVersion()

    @Input
    abstract Property<Arch> getArchitecture()

    @Input
    abstract Property<Boolean> getFallbackTo32Bit()

    @OutputFile
    abstract RegularFileProperty getDriverBinaryProperties()

    @OutputFile
    abstract RegularFileProperty getDriverBinary()

    @TaskAction
    void resolve() {
        def versionAndUri = new DriverUrlsConfiguration(driverUrlsConfiguration.get().asFile()).versionAndUriFor(downloadSpec())
        def cachedBinaryFile = resolveBinaryInCache(versionAndUri)

        copyFromCache(cachedBinaryFile)

        writeBinaryProperties(webDriverBinaryMetadata.systemProperty, versionAndUri.version, driverBinary.asFile.get().absolutePath)
    }

    @Internal
    protected OperatingSystem getOperatingSystem() {
        OperatingSystem.current()
    }

    @Internal
    protected String getBinaryFileName() {
        operatingSystem.getExecutableName(webDriverBinaryMetadata.binaryName)
    }

    private DriverDownloadSpecification downloadSpec() {
        DriverDownloadSpecification.builder()
            .name(webDriverBinaryMetadata.driverName)
            .version(version.get())
            .arch(architecture.get())
            .os(operatingSystem)
            .fallbackTo32Bit(fallbackTo32Bit.get())
            .build()
    }

    private File resolveBinaryInCache(VersionAndUri versionAndUri) {
        def installer = new DriverDistributionInstaller(project, downloadRoot.asFile.get(), webDriverBinaryMetadata.driverName, versionAndUri)
        def distributionRoot = installer.getDistributionRoot(versionAndUri.version).get()
        new File(distributionRoot, binaryFileName)
    }

    private void copyFromCache(File binaryFile) {
        fileSystemOperations.copy {
            from(binaryFile)

            def destination = driverBinary.get().asFile
            into(destination.parentFile)
            rename { destination.name }
        }
    }

    private void writeBinaryProperties(String propertyName, String version, String path) {
        driverBinaryProperties.get().asFile.withWriter { writer ->
            def properties = new Properties()
            properties.putAll(
                systemPropertyName: propertyName,
                version: version,
                path: path
            )
            properties.store(writer, null)
        }
    }

}

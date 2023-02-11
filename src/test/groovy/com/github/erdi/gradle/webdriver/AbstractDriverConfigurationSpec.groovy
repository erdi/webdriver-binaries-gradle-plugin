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
import org.ysb33r.grolifant.api.core.OperatingSystem
import spock.lang.Rollup
import spock.lang.Unroll

import static org.ysb33r.grolifant.api.core.OperatingSystem.Arch.X86
import static org.ysb33r.grolifant.api.core.OperatingSystem.Arch.X86_64

abstract class AbstractDriverConfigurationSpec extends PluginSpec {

    private final static String BINARY_CONTENT_DIR = 'webdriverBinary'
    private final static String BINARY_CONTENT_FILENAME = 'binaryContents.txt'

    abstract String getDriverName()

    String getDriverExecutableName() {
        driverName
    }

    String getDriverConfigurationBlockName() {
        driverName
    }

    @Rollup
    def 'can set driver urls configuration resource'() {
        given:
        def repository = setupRepository(driverName, driverExecutableName, version, os, arch)

        and:
        writeBuildScript()

        and:
        buildScript << """
            webdriverBinaries {
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath}')
                $driverConfigurationBlockName = '${version}'
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir "resolve${driverName.capitalize()}Binary"

        then:
        pluginDownloadedBinaryContents == repository.driverFileContents

        where:
        version = '2.42.0'
        os = OperatingSystem.current()
        arch = os.arch
    }

    @Unroll('can globally set architecture to #architecture')
    def 'can globally set architecture'() {
        given:
        def repository = setupRepository(driverName, driverExecutableName, version, os, architecture)

        and:
        writeBuildScript()

        and:
        buildScript << """
            webdriverBinaries {
                architecture = '${architecture.name()}'
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath}')
                $driverConfigurationBlockName {
                    version = '${version}'
                }
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir "resolve${driverName.capitalize()}Binary"

        then:
        pluginDownloadedBinaryContents == repository.driverFileContents

        where:
        architecture << DriverUrlsConfiguration.BITS.keySet()

        version = '2.42.0'
        os = OperatingSystem.current()
    }

    @Unroll('can set architecture to #architecture')
    def 'can set architecture'() {
        given:
        def repository = setupRepository(driverName, driverExecutableName, version, os, architecture)

        and:
        writeBuildScript()

        and:
        buildScript << """
            webdriverBinaries {
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath}')
                $driverConfigurationBlockName {
                    version = '${version}'
                    architecture = '${architecture.name()}'
                }
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir "resolve${driverName.capitalize()}Binary"

        then:
        pluginDownloadedBinaryContents == repository.driverFileContents

        where:
        architecture << DriverUrlsConfiguration.BITS.keySet()

        version = '2.42.0'
        os = OperatingSystem.current()
    }

    @Rollup
    def 'can globally enable fallback to 32 bit drivers when the 64 bit one is not found'() {
        given:
        def repository = setupRepository(driverName, driverExecutableName, version, os, X86)

        and:
        writeBuildScript()

        and:
        buildScript << """
            webdriverBinaries {
                fallbackTo32Bit = true
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath}')
                $driverConfigurationBlockName {
                    version = '${version}'
                    architecture = '${X86_64.name()}'
                }
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir "resolve${driverName.capitalize()}Binary"

        then:
        pluginDownloadedBinaryContents == repository.driverFileContents

        where:
        version = '2.42.0'
        os = OperatingSystem.current()
    }

    @Rollup
    def 'can enable fallback to 32 bit driver when the 64 bit one is not found on a per driver basis'() {
        given:
        def repository = setupRepository(driverName, driverExecutableName, version, os, X86)

        and:
        writeBuildScript()

        and:
        buildScript << """
            webdriverBinaries {
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath}')
                $driverConfigurationBlockName {
                    version = '${version}'
                    architecture = '${X86_64.name()}'
                    fallbackTo32Bit = true
                }
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir "resolve${driverName.capitalize()}Binary"

        then:
        pluginDownloadedBinaryContents == repository.driverFileContents

        where:
        version = '2.42.0'
        os = OperatingSystem.current()
    }

    private File writeBuildScript() {
        buildScript << """
            import com.github.erdi.gradle.webdriver.task.ResolveDriverBinary

            plugins {
                id 'com.github.erdi.webdriver-binaries'
            }

            tasks.withType(ResolveDriverBinary).all { task ->
                def finalizer = tasks.create("\${task.name}Finalizer", Copy) {
                    from(task.driverBinary)
                    into("\${buildDir}/$BINARY_CONTENT_DIR")
                    rename { '$BINARY_CONTENT_FILENAME' }
                }

                task.finalizedBy(finalizer)
            }
        """
    }

    private String getPluginDownloadedBinaryContents() {
        new File(testProjectDir, "build/$BINARY_CONTENT_DIR/$BINARY_CONTENT_FILENAME").text
    }

}

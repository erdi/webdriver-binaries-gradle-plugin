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

    private final static String BINARY_PATH_FILENAME = 'binaryPath.txt'

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
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath.replaceAll('\\\\', '/')}')
                $driverConfigurationBlockName = '${version}'
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir 'configureBinaries'

        then:
        pluginDownloadedBinaryContents == repository.driverFileContents

        where:
        version = '2.42.0'
        os = OperatingSystem.current()
        arch = os.arch
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
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath.replaceAll('\\\\', '/')}')
                $driverConfigurationBlockName {
                    version = '${version}'
                    architecture = '${architecture.name()}'
                }
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir 'configureBinaries'

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
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath.replaceAll('\\\\', '/')}')
                $driverConfigurationBlockName {
                    version = '${version}'
                    architecture = '${X86_64.name()}'
                }
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir 'configureBinaries'

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
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath.replaceAll('\\\\', '/')}')
                $driverConfigurationBlockName {
                    version = '${version}'
                    architecture = '${X86_64.name()}'
                    fallbackTo32Bit = true
                }
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir 'configureBinaries'

        then:
        pluginDownloadedBinaryContents == repository.driverFileContents

        where:
        version = '2.42.0'
        os = OperatingSystem.current()
    }

    private File writeBuildScript() {
        buildScript << """
            import com.github.erdi.gradle.webdriver.task.ConfigureBinary

            plugins {
                id 'com.github.erdi.webdriver-binaries'
            }

            tasks.withType(ConfigureBinary).all { task ->
                task.addBinaryAware { binaryPath ->
                    buildDir.mkdirs()
                    new File(buildDir, '$BINARY_PATH_FILENAME') << binaryPath
                }
            }

            task configureBinaries {
                dependsOn tasks.withType(ConfigureBinary)
            }
        """
    }

    private String getPluginDownloadedBinaryContents() {
        def path = new File(testProjectDir, "build/$BINARY_PATH_FILENAME").text
        new File(path).text
    }

}

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
package com.github.erdi.gradle.webdriver

import org.ysb33r.grolifant.api.OperatingSystem
import org.ysb33r.grolifant.api.OperatingSystem.Arch
import org.ysb33r.grolifant.api.os.MacOsX

import static org.ysb33r.grolifant.api.OperatingSystem.Arch.X86

class DriverDistributionInstallerSpec extends PluginSpec {

    void setup() {
        buildScript << """
            import com.github.erdi.gradle.webdriver.*
            import com.github.erdi.gradle.webdriver.chrome.*
            import org.ysb33r.grolifant.api.os.*
            import org.ysb33r.grolifant.api.*

            plugins {
                id 'com.github.erdi.webdriver-binaries'
            }
        """
    }

    def 'can successfully install a version of a driver'() {
        given:
        writeTestDriverDistributionInstallerSource(driverName)
        def repository = setupRepository(driverName, driverName, version, os, arch)

        and:
        writeOutputBinaryPathTask(repository.configurationFile, version, os, arch)

        when:
        runTasksWithUniqueGradleHomeDir 'outputBinaryPath'

        then:
        downloadedBinaryFile(os.getExecutableName(driverName), os).text == repository.driverFileContents

        where:
        driverName = 'testdriver'
        version = '2.42.0'
        os = MacOsX.INSTANCE
        arch = X86
    }

    private void writeTestDriverDistributionInstallerSource(String driverName) {
        buildScript << """
            class TestDriverDistributionInstaller extends DriverDistributionInstaller {
                TestDriverDistributionInstaller(Project project, TextResource repositoryResource, String distributionVersion, OperatingSystem os, OperatingSystem.Arch arch) {
                    super(project, "$driverName", repositoryResource, null, distributionVersion, os, arch, false)
                }
            }
        """
    }

    private void writeOutputBinaryPathTask(File configurationFile, String version, OperatingSystem os, Arch arch) {
        def configurationFilePath = configurationFile.absolutePath
        def osCode = "${os.class.simpleName}.INSTANCE"
        def archCode = "OperatingSystem.Arch.${arch.name()}"
        def code = "new TestDriverDistributionInstaller(project, resources.text.fromFile('$configurationFilePath'), '$version', $osCode, $archCode)"

        writeOutputBinaryPathTask(code)
    }
}

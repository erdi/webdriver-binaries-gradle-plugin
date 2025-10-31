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

import org.ysb33r.grolifant.api.core.OperatingSystem
import org.ysb33r.grolifant.api.core.OperatingSystem.Arch
import org.ysb33r.grolifant.api.core.os.MacOsX
import spock.lang.Rollup

import static org.ysb33r.grolifant.api.core.OperatingSystem.Arch.X86

class DriverDistributionInstallerSpec extends PluginSpec {

    void setup() {
        buildScript << """
            import com.github.erdi.gradle.webdriver.*
            import com.github.erdi.gradle.webdriver.chrome.*
            import com.github.erdi.gradle.webdriver.repository.*
            import org.ysb33r.grolifant.api.core.os.*
            import org.ysb33r.grolifant.api.core.*

            plugins {
                id 'com.github.erdi.webdriver-binaries'
            }
        """
    }

    @Rollup
    def 'can successfully install a version of a driver'() {
        given:
        def repository = setupRepository(driverName, driverName, version, os, arch)

        and:
        writeOutputBinaryPathTask(repository.configurationFile, driverName, version, os, arch)

        when:
        runTasksWithUniqueGradleHomeDir 'outputBinaryPath'

        then:
        downloadedBinaryFile(os.getExecutableNames(driverName)?.first(), os).text == repository.driverFileContents

        where:
        driverName = 'testdriver'
        version = '2.42.0'
        os = MacOsX.INSTANCE
        arch = X86
    }

    private void writeOutputBinaryPathTask(File configurationFile, String driverName, String version, OperatingSystem os, Arch arch) {
        def configurationFilePath = configurationFile.absolutePath.replaceAll('\\\\', '/')
        def osCode = "${os.class.simpleName}.INSTANCE"
        def archCode = "OperatingSystem.Arch.${arch.name()}"
        def downloadSpecCode = "DriverDownloadSpecification.builder().name('$driverName').version('$version').os($osCode).arch($archCode).build()"
        def versionAndUriCode = "new DriverUrlsConfiguration(resources.text.fromFile('$configurationFilePath').asFile()).versionAndUriFor($downloadSpecCode)"
        def code = """
            new DriverDistributionInstaller(
                project,
                gradle.gradleUserHomeDir,
                '$driverName',
                $versionAndUriCode
            )
        """

        writeOutputBinaryPathTask(code)
    }

}

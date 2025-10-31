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

import com.github.erdi.gradle.webdriver.repository.DriverUrlsConfiguration
import groovy.json.JsonOutput
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.grolifant.api.core.OperatingSystem
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PluginSpec extends Specification {

    private final static String DISTRIBUTION_ROOT_PATH_FILENAME = 'distributionRootPath.txt'

    @TempDir
    protected File testProjectDir

    @TempDir
    protected File gradleHomeDir

    @TempDir
    protected File downloadRoot

    @TempDir
    protected File driverRepository

    protected File buildScript

    void setup() {
        buildScript = new File(testProjectDir, 'build.gradle')
    }

    protected writeOutputBinaryPathTask(String installerConstructorCode) {
        buildScript << """
            task outputBinaryPath {
                doLast {
                    buildDir.mkdirs()
                    def installer = $installerConstructorCode

                    new File(buildDir, '$DISTRIBUTION_ROOT_PATH_FILENAME') << installer.getDistributionRoot(null).get().absolutePath
                }
            }
        """
    }

    protected BuildResult runTasksWithUniqueGradleHomeDir(String... taskNames) {
        runBuild('--gradle-user-home', gradleHomeDir.absolutePath, *taskNames)
    }

    protected BuildResult runTasks(String... taskNames) {
        runBuild(*taskNames)
    }

    private BuildResult runBuild(String... arguments) {
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('--stacktrace', *arguments)
            .withPluginClasspath()
            .forwardOutput()
            .build()
    }

    protected File getDistributionRoot() {
        def distributionPathRootPath = new File(testProjectDir, "build/$DISTRIBUTION_ROOT_PATH_FILENAME").text
        new File(distributionPathRootPath)
    }

    protected File downloadedBinaryFile(String binaryName, OperatingSystem operatingSystem = OperatingSystem.current()) {
        new File(distributionRoot, operatingSystem.getExecutableNames(binaryName)?.first())
    }

    @SuppressWarnings(['SpaceAfterClosingBrace', 'SpaceBeforeClosingBrace'])
    protected Repository setupRepository(
        String name, String binaryName = name, String version, OperatingSystem operatingSystem = OperatingSystem.current(),
        OperatingSystem.Arch arch = OperatingSystem.current().arch
    ) {
        String driverFileContents = "${name}_${operatingSystem.class.simpleName}_${arch.name()}_$version"
        def driverZip = writeDriverZip(operatingSystem.getExecutableNames(binaryName)?.first(), driverFileContents)

        def configurationFile = new File(driverRepository, 'repository.json') << JsonOutput.toJson(
            drivers: [
                [
                    name: name,
                    platform: DriverUrlsConfiguration.PLATFORMS[operatingSystem],
                    bit: DriverUrlsConfiguration.BITS[arch],
                    version: version,
                    url: driverZip.toURI().toString(),
                    arch: DriverUrlsConfiguration.ARCHS[arch]
                ]
            ]
        )

        new Repository(configurationFile: configurationFile, driverFileContents: driverFileContents)
    }

    @SuppressWarnings(['SpaceAfterClosingBrace', 'SpaceBeforeClosingBrace'])
    private File writeDriverZip(String driverFilename, String driverFileContents) {
        def zipFile = new File(driverRepository, "${driverFileContents}.zip")

        new ZipOutputStream(new FileOutputStream(zipFile)).withCloseable { stream ->
            stream.putNextEntry(new ZipEntry(driverFilename))
            stream.write(driverFileContents.getBytes(StandardCharsets.UTF_8))
            stream.closeEntry()
        }

        zipFile
    }

    protected static class Repository {

        File configurationFile
        String driverFileContents

    }

}

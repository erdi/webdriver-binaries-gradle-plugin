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
package com.energizedwork.gradle.webdriver

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ysb33r.grolifant.api.OperatingSystem
import spock.lang.Specification

class PluginSpec extends Specification {

    private final static String DISTRIBUTION_ROOT_PATH_FILENAME = 'distributionRootPath.txt'

    @Rule
    protected TemporaryFolder testProjectDir

    @Rule
    protected TemporaryFolder gradleHomeDir

    @Rule
    protected TemporaryFolder downloadRoot

    protected File buildScript

    void setup() {
        buildScript = testProjectDir.newFile('build.gradle')
    }

    protected writeOutputBinaryPathTask(String installerConstructorCode) {
        buildScript << """
            task outputBinaryPath {
                doLast {
                    buildDir.mkdirs()
                    def installer = $installerConstructorCode

                    new File(buildDir, '$DISTRIBUTION_ROOT_PATH_FILENAME') << installer.distributionRoot.absolutePath
                }
            }
        """
    }

    protected BuildResult runTasksWithUniqueGradleHomeDir(String... taskNames) {
        runBuild('--gradle-user-home', gradleHomeDir.root.absolutePath, *taskNames)
    }

    protected BuildResult runTasks(String... taskNames) {
        runBuild(*taskNames)
    }

    private BuildResult runBuild(String... arguments) {
        GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('--stacktrace', *arguments)
            .withPluginClasspath()
            .forwardOutput()
            .build()
    }

    protected File getDistributionRoot() {
        def distributionPathRootPath = new File(testProjectDir.root, "build/$DISTRIBUTION_ROOT_PATH_FILENAME").text
        new File(distributionPathRootPath)
    }

    protected File downloadedBinaryFile(String binaryName, OperatingSystem operatingSystem = OperatingSystem.current()) {
        new File(distributionRoot, operatingSystem.getExecutableName(binaryName))
    }

    protected static <T> List<T> pickRandomly(int count, List<T> items) {
        def copy = items.toList()
        Collections.shuffle(copy)
        copy.take(count)
    }

}

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

import com.energizedwork.gradle.webdriver.chrome.ChromeDriverDistributionInstaller
import com.energizedwork.gradle.webdriver.gecko.GeckoDriverDistributionInstaller
import org.ysb33r.gradle.olifant.OperatingSystem
import spock.lang.Unroll

import static BinariesVersions.LATEST_CHROMEDRIVER_VERSION
import static BinariesVersions.LATEST_GECKODRVIER_VERSION

class IdeaJUnitPluginIntegrationSpec extends PluginSpec {

    static final String TEST_PROJECT_NAME = 'idea-test'

    @Unroll('default JUnit run configuration in IntelliJ is configured with the downloaded #binaryName binary')
    def "default JUnit run configuration in IntelliJ is configured with the downloaded webdriver binary"() {
        given:
        setupProjectName()
        buildScript << """
            import $installerClass.name
            import org.ysb33r.gradle.olifant.OperatingSystem

            plugins {
                id 'com.energizedwork.webdriver-binaries'
                id 'com.energizedwork.idea-junit' version '1.2'
            }

            webdriverBinaries {
                $binaryName '$binaryVersion'
            }
        """
        writeOutputBinaryPathTask(installerClass, binaryVersion)

        when:
        runTasksWithUniqueGradleHomeDir 'ideaWorkspace', 'outputBinaryPath'
        def binaryFile = new File(distributionRoot, OperatingSystem.current().getExecutableName(binaryName))

        then:
        junitConfVmParams == "-D$systemProperty=$binaryFile.absolutePath"

        where:
        binaryName     | binaryVersion               | systemProperty            | installerClass
        'chromedriver' | LATEST_CHROMEDRIVER_VERSION | 'webdriver.chrome.driver' | ChromeDriverDistributionInstaller
        'geckodriver'  | LATEST_GECKODRVIER_VERSION  | 'webdriver.gecko.driver'  | GeckoDriverDistributionInstaller
    }

    private void setupProjectName() {
        testProjectDir.newFile('settings.gradle') << """
            rootProject.name = '$TEST_PROJECT_NAME'
        """
    }

    private Node parseJunitConf() {
        def node = new XmlParser().parse(new File(testProjectDir.root, "${TEST_PROJECT_NAME}.iws"))
        def runManager = node.component.find { it.@name == 'RunManager' }
        runManager.configuration.find { it.@default == 'true' && it.'@type' == 'JUnit' }
    }

    private String getJunitConfVmParams() {
        parseJunitConf().option.find { it.@name == 'VM_PARAMETERS' }.@value
    }

}

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
import com.energizedwork.gradle.webdriver.ie.InternetExplorerDriverServerDistributionInstaller
import org.ysb33r.grolifant.api.OperatingSystem
import org.ysb33r.grolifant.api.os.Windows
import spock.lang.Unroll

import static BinariesVersions.LATEST_CHROMEDRIVER_VERSION
import static BinariesVersions.LATEST_GECKODRVIER_VERSION
import static com.energizedwork.gradle.webdriver.BinariesVersions.LATEST_IEDRIVERSERVER_VERSION

class IdeaJUnitPluginIntegrationSpec extends PluginSpec {

    static final String TEST_PROJECT_NAME = 'idea-test'

    @Unroll('default JUnit run configuration in IntelliJ is configured with the downloaded #binaryName binary')
    def "default JUnit run configuration in IntelliJ is configured with the downloaded webdriver binary"() {
        given:
        setupProjectName()
        buildScript << """
            import $installerClass.name
            import org.ysb33r.grolifant.api.OperatingSystem

            plugins {
                id 'com.energizedwork.webdriver-binaries'
                id 'com.energizedwork.idea-junit' version '1.2'
            }

            webdriverBinaries {
                ${binaryName.toLowerCase()} '$binaryVersion'
            }
        """
        writeOutputBinaryPathTask(installerClass, binaryVersion, osProvidingCode)

        when:
        runTasksWithUniqueGradleHomeDir 'ideaWorkspace', 'outputBinaryPath'
        def binaryFile = downloadedBinaryFile(binaryName, os)

        then:
        junitConfVmParams == "-D$systemProperty=$binaryFile.absolutePath"

        where:
        binaryName       | binaryVersion                 | systemProperty            | installerClass                                    | os                        | osProvidingCode
        'chromedriver'   | LATEST_CHROMEDRIVER_VERSION   | 'webdriver.chrome.driver' | ChromeDriverDistributionInstaller                 | OperatingSystem.current() | 'OperatingSystem.current()'
        'geckodriver'    | LATEST_GECKODRVIER_VERSION    | 'webdriver.gecko.driver'  | GeckoDriverDistributionInstaller                  | OperatingSystem.current() | 'OperatingSystem.current()'
        'IEDriverServer' | LATEST_IEDRIVERSERVER_VERSION | 'webdriver.ie.driver'     | InternetExplorerDriverServerDistributionInstaller | Windows.INSTANCE          | null
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

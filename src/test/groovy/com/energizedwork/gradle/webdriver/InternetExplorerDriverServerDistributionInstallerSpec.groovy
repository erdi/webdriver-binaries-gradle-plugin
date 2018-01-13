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

import com.energizedwork.gradle.webdriver.ie.InternetExplorerDriverServerDistributionInstaller
import org.ysb33r.grolifant.api.os.Windows
import spock.lang.Unroll

import static com.energizedwork.gradle.webdriver.BinariesVersions.LATEST_MINOR_IEDRIVER_VERSION_NUMBER

class InternetExplorerDriverServerDistributionInstallerSpec extends PluginSpec {

    @Unroll('can install iedriverserver #version')
    def 'can successfully install selected versions of iedriverserver'(String version) {
        given:
        buildScript << """
            import com.energizedwork.gradle.webdriver.ie.InternetExplorerDriverServerDistributionInstaller
            import org.ysb33r.grolifant.api.os.*

            plugins {
                id 'com.energizedwork.webdriver-binaries'
            }

            webdriverBinaries {
                downloadRoot(new File('${downloadRoot.root.absolutePath}'))
            }
        """
        writeOutputBinaryPathTask(InternetExplorerDriverServerDistributionInstaller, version)

        when:
        runTasks 'outputBinaryPath'

        then:
        downloadedBinaryFile('IEDriverServer', Windows.INSTANCE).exists()

        where:
        version << selectedVersions()
    }

    static List<String> selectedVersions() {
        def twoLineVersions = (39..53).collect { "2.${it}.0" }
        def threeLineVersions = (0..LATEST_MINOR_IEDRIVER_VERSION_NUMBER).collect { "3.${it}.0" }

        pickRandomly(10, twoLineVersions + threeLineVersions)
    }

}

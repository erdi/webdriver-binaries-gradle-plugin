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

import org.ysb33r.grolifant.api.OperatingSystem
import org.ysb33r.grolifant.api.os.Linux
import org.ysb33r.grolifant.api.os.MacOsX
import org.ysb33r.grolifant.api.os.Windows
import spock.lang.Unroll

import static BinariesVersions.LATEST_MINOR_CHROMEDRIVER_VERSION_NUMBER

class ChromeDriverDistributionInstallerSpec extends PluginSpec {

    @Unroll('can install chromedriver #version on #os.class.simpleName')
    def 'can successfully install selected versions of chromedriver across different operating systems'(String version, OperatingSystem os) {
        given:
        buildScript << """
            import com.energizedwork.gradle.webdriver.chrome.ChromeDriverDistributionInstaller
            import org.ysb33r.grolifant.api.os.*

            plugins {
                id 'com.energizedwork.webdriver-binaries'
            }

            webdriverBinaries {
                downloadRoot(new File('${downloadRoot.root.absolutePath}'))
                chromedriver '$version'
            }
        """

        and:
        writeOutputBinaryPathTask "new ChromeDriverDistributionInstaller(project, null, '$version', ${os.class.simpleName}.INSTANCE)"

        when:
        runTasks 'outputBinaryPath'

        then:
        downloadedBinaryFile('chromedriver', os).exists()

        where:
        [version, os] << selectedVersionsAcrossDifferentOperatingSystems()
    }

    static List<List<?>> selectedVersionsAcrossDifferentOperatingSystems() {
        def allVersions = (0..LATEST_MINOR_CHROMEDRIVER_VERSION_NUMBER).collectMany { version ->
            [MacOsX.INSTANCE, Windows.INSTANCE, Linux.INSTANCE].collect { operatingSystem ->
                ["2.$version", operatingSystem]
            }
        }
        def unsupportedVersions = [['2.5', Windows.INSTANCE]]

        pickRandomly(15, allVersions - unsupportedVersions)
    }

}

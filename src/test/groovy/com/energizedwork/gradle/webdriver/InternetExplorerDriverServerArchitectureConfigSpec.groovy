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
package com.energizedwork.gradle.webdriver

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Unroll

import java.util.zip.ZipFile

import static com.energizedwork.gradle.webdriver.BinariesVersions.LATEST_IEDRIVERSERVER_VERSION

class InternetExplorerDriverServerArchitectureConfigSpec extends PluginSpec {

    private final static String BINARY_PATH_FILENAME = 'binaryPath.txt'

    @Rule
    TemporaryFolder temporaryFolder

    @Unroll
    def 'can specify architecture when configuring iedriverserver'() {
        given:
        buildScript << """
            plugins {
                id 'com.energizedwork.webdriver-binaries'
            }

            webdriverBinaries {
                iedriverserver {
                    version = '${LATEST_IEDRIVERSERVER_VERSION}'
                    architecture = '$architecture'
                }
            }

            configureIeDriverServerBinary {
                addBinaryAware { binaryPath ->
                    buildDir.mkdirs()
                    new File(buildDir, '$BINARY_PATH_FILENAME') << binaryPath
                }
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir 'configureIeDriverServerBinary'

        then:
        pluginDownloadedBinaryBytes == latestIeDriverServerBinaryBytes(archFilenamePart)

        where:
        architecture | archFilenamePart
        'X86'        | 'Win32'
        'X86_64'     | 'x64'
    }

    byte[] latestIeDriverServerBinaryBytes(String archFilenamePart) {
        def url = "http://selenium-release.storage.googleapis.com/${stripMinorVersion(LATEST_IEDRIVERSERVER_VERSION)}/IEDriverServer_${archFilenamePart}_${LATEST_IEDRIVERSERVER_VERSION}.zip".toURL()
        def downloaded = temporaryFolder.newFile() << url.newInputStream()

        def zipFile = new ZipFile(downloaded)
        zipFile.getInputStream(zipFile.entries().toList().first()).bytes
    }

    byte[] getPluginDownloadedBinaryBytes() {
        def path = new File(testProjectDir.root, "build/$BINARY_PATH_FILENAME").text
        new File(path).bytes
    }

    private String stripMinorVersion(String version) {
        version[0..<version.lastIndexOf('.')]
    }

}

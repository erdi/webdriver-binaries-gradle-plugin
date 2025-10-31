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

import spock.lang.Unroll

class ExtendedIdeaPluginIntegrationSpec extends PluginSpec {

    private final static String BINARY_PATH_FILENAME = 'binaryPath.txt'

    static final String TEST_PROJECT_NAME = 'idea-test'

    @Unroll('default JUnit run configuration in IntelliJ is configured with the downloaded #driverConfigurationBlockName binary')
    def "default JUnit run configuration in IntelliJ is configured with the downloaded webdriver binary"() {
        given:
        def repository = setupRepository(driverName, binaryName, version)
        setupProjectName()
        writeBuildScript(configureTask)

        and:
        buildScript << """
            webdriverBinaries {
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath.replaceAll('\\\\', '/')}')
                $driverConfigurationBlockName '$version'
            }
        """

        when:
        runTasksWithUniqueGradleHomeDir 'ideaWorkspace'

        then:
        pluginDownloadedBinaryPath

        and:
        junitConfVmParams == "-D$systemProperty=$pluginDownloadedBinaryPath"

        where:
        driverConfigurationBlockName | driverName               | binaryName       | systemProperty            | configureTask
        'chromedriver'               | 'chromedriver'           | 'chromedriver'   | 'webdriver.chrome.driver' | 'configureChromeDriverBinary'
        'geckodriver'                | 'geckodriver'            | 'geckodriver'    | 'webdriver.gecko.driver'  | 'configureGeckoDriverBinary'
        'edgedriver'                 | 'edgedriver'             | 'msedgedriver'   | 'webdriver.edge.driver'   | 'configureEdgeDriverBinary'

        version = '1.2.3'
    }

    private void writeBuildScript(String configureTask) {
        buildScript << """
            plugins {
                id 'com.github.erdi.webdriver-binaries'
                id 'com.github.erdi.extended-idea' version '2.0'
            }

            $configureTask {
                addBinaryAware { binaryPath ->
                    buildDir.mkdirs()
                    new File(buildDir, '$BINARY_PATH_FILENAME') << binaryPath
                }
            }
        """
    }

    private void setupProjectName() {
        new File(testProjectDir, 'settings.gradle') << """
            rootProject.name = '$TEST_PROJECT_NAME'
        """
    }

    @SuppressWarnings(['SpaceAfterClosingBrace', 'SpaceBeforeClosingBrace'])
    private Node parseJunitConf() {
        def node = new XmlParser().parse(new File(testProjectDir, "${TEST_PROJECT_NAME}.iws"))
        def runManager = node.component.find { it.@name == 'RunManager' }
        runManager.configuration.find { it.@default == 'true' && it.'@type' == 'JUnit' }
    }

    @SuppressWarnings(['SpaceAfterClosingBrace'])
    private String getJunitConfVmParams() {
        parseJunitConf().option.find { it.@name == 'VM_PARAMETERS' }.@value
    }

    private String getPluginDownloadedBinaryPath() {
        new File(testProjectDir, "build/$BINARY_PATH_FILENAME").text
    }

}

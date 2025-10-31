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
package com.github.erdi.gradle.webdriver

import org.ysb33r.grolifant.api.core.OperatingSystem
import spock.lang.Unroll

import static com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata.CHROMEDRIVER
import static com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata.EDGEDRIVER
import static com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata.GECKODRIVER

class ConfigureJavaForkOptionsTaskSpec extends PluginSpec {

    private final static String BINARY_PATH_FILENAME = 'binaryPath.txt'

    @Unroll
    def 'can configure additional tasks implementing JavaForkOptions'() {
        given:
        def repository = setupRepository(driverName, driverExecutableName, version, os, arch)

        and:
        writeMainClass()

        and:
        buildScript << """
            import com.github.erdi.gradle.webdriver.task.ConfigureBinary

            plugins {
                id 'java'
                id 'com.github.erdi.webdriver-binaries'
            }

            webdriverBinaries {
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath.replaceAll('\\\\', '/')}')
                $driverConfigurationBlockName = '${version}'
            }

            task exec(type: JavaExec) {
                classpath = sourceSets.main.runtimeClasspath

                main = 'Main'

                args(new File(buildDir, '$BINARY_PATH_FILENAME').absolutePath, '$systemProperty')
            }

            webdriverBinaries.configureTask(exec)
        """

        when:
        runTasksWithUniqueGradleHomeDir 'exec'

        then:
        pluginDownloadedBinaryContents == repository.driverFileContents

        where:
        driverName               | driverExecutableName | driverConfigurationBlockName | systemProperty
        'chromedriver'           | 'chromedriver'       | 'chromedriver'               | CHROMEDRIVER.systemProperty
        'geckodriver'            | 'geckodriver'        | 'geckodriver'                | GECKODRIVER.systemProperty
        'edgedriver'             | 'msedgedriver'       | 'edgedriver'                 | EDGEDRIVER.systemProperty

        version = '2.42.0'
        os = OperatingSystem.current()
        arch = os.arch
    }

    private void writeMainClass() {
        def sourceDir = new File(testProjectDir, 'src/main/java')
        sourceDir.mkdirs()
        new File(sourceDir, 'Main.java') << """
            import java.nio.file.Files;
            import java.nio.file.Paths;

            public class Main {
                public static void main(String[] args) throws Exception {
                    String configuredSystemProperty = System.getProperty(args[1]);
                    Files.write(Paths.get(args[0]), configuredSystemProperty.getBytes());
                }
            }
        """
    }

    private String getPluginDownloadedBinaryContents() {
        def path = new File(testProjectDir, "build/$BINARY_PATH_FILENAME").text
        new File(path).text
    }

}


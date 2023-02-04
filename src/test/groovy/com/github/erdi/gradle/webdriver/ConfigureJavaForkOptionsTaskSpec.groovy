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

import static com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata.*
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class ConfigureJavaForkOptionsTaskSpec extends PluginSpec {

    private final static String BINARY_PATH_FILENAME = 'binaryPath.txt'

    @Unroll
    def 'can configure tasks implementing JavaForkOptions'() {
        given:
        def repository = setupRepository(driverName, driverExecutableName, version, os, arch)

        and:
        writeMainClassOutputtingConfiguredPathContentsToAFile()

        and:
        buildScript << """
            plugins {
                id 'java'
                id 'com.github.erdi.webdriver-binaries'
            }

            webdriverBinaries {
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath}')
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

    def "resolve binaries tasks are not part of the task graph when no driver versions are specified"() {
        given:
        writeEmptyMainClass()

        and:
        buildScript << """
            plugins {
                id 'java'
                id 'com.github.erdi.webdriver-binaries'
            }

            task exec(type: JavaExec) {
                classpath = sourceSets.main.runtimeClasspath

                main = 'Main'
            }

            webdriverBinaries.configureTask(exec)
        """

        when:
        def result = runTasks 'exec'

        then:
        result.tasks*.path == [':compileJava', ':processResources', ':classes', ':exec']
    }

    @Unroll
    @SuppressWarnings(['GStringExpressionWithinString'])
    def "binary path is not an input for a configured JavaForkOptions task"() {
        given:
        def repository = setupRepository(driverName, driverExecutableName, version, os, arch)

        and:
        writeEmptyMainClass()

        and:
        buildScript << """
            plugins {
                id 'java'
                id 'com.github.erdi.webdriver-binaries'
            }

            webdriverBinaries {
                driverUrlsConfiguration = resources.text.fromFile('${repository.configurationFile.absolutePath}')
                $driverConfigurationBlockName = '${version}'
            }

            task exec(type: JavaExec) {
                classpath = sourceSets.main.runtimeClasspath

                main = 'Main'

                outputs.upToDateWhen { true }
            }

            webdriverBinaries.configureTask(exec)
        """

        and:
        runTasks 'exec'

        and:
        buildScript << '''
            import com.github.erdi.gradle.webdriver.task.ResolveDriverBinary

            tasks.withType(ResolveDriverBinary).configureEach { task ->
                task.driverBinary.set(project.layout.buildDirectory.file("${task.name}/binary-copy"))
            }
        '''

        when:
        def subsequentBuildResult = runTasks 'exec'

        then:
        subsequentBuildResult.task(':exec').outcome == UP_TO_DATE

        where:
        driverName               | driverExecutableName | driverConfigurationBlockName | systemProperty
        'chromedriver'           | 'chromedriver'       | 'chromedriver'               | CHROMEDRIVER.systemProperty
        'geckodriver'            | 'geckodriver'        | 'geckodriver'                | GECKODRIVER.systemProperty
        'edgedriver'             | 'msedgedriver'       | 'edgedriver'                 | EDGEDRIVER.systemProperty

        version = '2.42.0'
        os = OperatingSystem.current()
        arch = os.arch
    }

    private void writeEmptyMainClass() {
        writeMainClass '''
            public class Main {
                public static void main(String[] args) throws Exception {
                }
            }
        '''
    }

    private void writeMainClassOutputtingConfiguredPathContentsToAFile() {
        writeMainClass '''
            import java.nio.file.Files;
            import java.nio.file.Paths;

            public class Main {
                public static void main(String[] args) throws Exception {
                    String configuredSystemProperty = System.getProperty(args[1]);
                    Files.write(Paths.get(args[0]), configuredSystemProperty.getBytes());
                }
            }
        '''
    }

    private void writeMainClass(String code) {
        def sourceDir = new File(testProjectDir, 'src/main/java')
        sourceDir.mkdirs()
        new File(sourceDir, 'Main.java') << code
    }

    private String getPluginDownloadedBinaryContents() {
        def path = new File(testProjectDir, "build/$BINARY_PATH_FILENAME").text
        new File(path).text
    }

}


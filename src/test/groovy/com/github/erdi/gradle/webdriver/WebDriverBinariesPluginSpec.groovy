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

import org.ysb33r.grolifant.api.core.OperatingSystem
import spock.lang.Unroll

import static com.github.erdi.gradle.webdriver.BinariesVersions.*

class WebDriverBinariesPluginSpec extends PluginSpec {

    static List<List<?>> parameters() {
        def parameters = [
            ['geckodriver', TESTED_GECKODRVIER_VERSION, 'selenium-firefox-driver']
        ]
        // there are no current chrome or edge versions in https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository/blob/master/repository-3.0.json

//        if (OperatingSystem.current().windows) {
//            parameters << [
//                'edgedriver', TESTED_EDGEDRIVER_VERSION, 'selenium-edge-driver'
//            ]
//        } else {
//            parameters << [
//                'chromedriver', TESTED_CHROMEDRIVER_VERSION, 'selenium-chrome-driver'
//            ]
//        }
        parameters
    }

    @Unroll('#binaryName binary is downloaded and test task is configured as per plugin config')
    void 'binary is downloaded and test task is configured as per plugin config'() {
        given:
        writeBuild(binaryName, binaryVersion, seleniumModule)
        writeRatpackApplication()
        writeGebSpec()

        when:
        runTasks 'test'

        then:
        noExceptionThrown()

        where:
        [binaryName, binaryVersion, seleniumModule] << parameters()
    }

    private void writeBuild(String binaryName, String binaryVersion, String seleniumModule) {
        buildScript << """
            plugins {
                id 'com.github.erdi.webdriver-binaries'
                id 'groovy'
                id 'io.ratpack.ratpack-java' version '1.9.0'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation 'org.seleniumhq.selenium:$seleniumModule:4.7.2'
            }

            webdriverBinaries {
                downloadRoot = new File('${downloadRoot.absolutePath.replace('\\', '\\\\')}')
                $binaryName {
                    version = '$binaryVersion'
                    ${architectureCode(binaryName)}
                }
            }

            test {
                useJUnitPlatform()
                testLogging {
                    exceptionFormat 'full'
                }
            }
        """
    }

    private String architectureCode(String binaryName) {
        if (binaryName == 'chromedriver' && OperatingSystem.current().windows) {
            'architecture = "X86"'
        } else {
            ''
        }
    }

    private void writeRatpackApplication() {
        buildScript << '''
            dependencies {
                testImplementation ratpack.dependency('test')
            }
        '''
        def sourceDir = new File(testProjectDir, 'src/main/java')
        sourceDir.mkdirs()
        new File(sourceDir, 'App.java') << """
            import ratpack.server.RatpackServer;
            import ratpack.http.MediaType;

            public class App {
                public static void main(String[] args) throws Exception {
                    RatpackServer.start(s ->
                        s.handlers(chain ->
                            chain.all(ctx -> ctx.getResponse().send(MediaType.TEXT_HTML, "<html><body>Hello World!</body></html>"))
                        )
                    );
                }
            }
        """
    }

    private void writeGebSpec() {
        buildScript << '''
            dependencies {
                testImplementation 'org.gebish:geb-spock:7.0'
            }
        '''
        def testDir = new File(testProjectDir, 'src/test/groovy')
        testDir.mkdirs()
        new File(testDir, 'AppSpec.groovy') << '''
            import ratpack.test.MainClassApplicationUnderTest
            import spock.lang.AutoCleanup
            import geb.spock.GebSpec

            class AppSpec extends GebSpec {

                @AutoCleanup
                def applicationUnderTest = new MainClassApplicationUnderTest(App)

                def 'can drive the browser'() {
                    when:
                    go applicationUnderTest.address.toString()

                    then:
                    $().text() == 'Hello World!'
                }

            }
        '''
    }

}

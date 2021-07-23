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

import com.github.erdi.gradle.webdriver.category.EndToEnd
import org.junit.experimental.categories.Category
import org.ysb33r.grolifant.api.OperatingSystem
import spock.lang.Requires
import spock.lang.Unroll

import static BinariesVersions.TESTED_CHROMEDRIVER_VERSION
import static BinariesVersions.TESTED_GECKODRVIER_VERSION
import static BinariesVersions.TESTED_IEDRIVERSERVER_VERSION
import static com.github.erdi.gradle.webdriver.BinariesVersions.TESTED_EDGEDRIVER_VERSION

@Category(EndToEnd)
class WebDriverBinariesPluginSpec extends PluginSpec {

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
        binaryName     | binaryVersion               | seleniumModule
        'chromedriver' | TESTED_CHROMEDRIVER_VERSION | 'selenium-chrome-driver'
        'geckodriver'  | TESTED_GECKODRVIER_VERSION  | 'selenium-firefox-driver'
    }

    @Requires({ OperatingSystem.current().windows })
    @Unroll('#binaryName binary is downloaded and test task is configured as per plugin config')
    void 'windows specific binary is downloaded and test task is configured as per plugin config'() {
        given:
        writeBuild(binaryName, binaryVersion, seleniumModule)
        writeRatpackApplication()
        writeGebSpec()

        when:
        runTasks 'test'

        then:
        noExceptionThrown()

        where:
        binaryName       | binaryVersion                 | seleniumModule
        'iedriverserver' | TESTED_IEDRIVERSERVER_VERSION | 'selenium-ie-driver'
        'edgedriver'     | TESTED_EDGEDRIVER_VERSION     | 'selenium-edge-driver'
    }

    private void writeBuild(String binaryName, String binaryVersion, String seleniumModule) {
        buildScript << """
            plugins {
                id 'com.github.erdi.webdriver-binaries'
                id 'groovy'
                id 'io.ratpack.ratpack-java' version '1.5.0'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testCompile 'org.seleniumhq.selenium:$seleniumModule:3.5.3'
            }

            webdriverBinaries {
                downloadRoot(new File('${downloadRoot.root.absolutePath.replace('\\', '\\\\')}'))
                $binaryName {
                    version = '$binaryVersion'
                    ${architectureCode(binaryName)}
                }
            }

            test {
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
                testCompile ratpack.dependency('test')
            }
        '''
        new File(testProjectDir.newFolder('src', 'main', 'java'), 'App.java') << """
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
                testCompile 'org.codehaus.groovy:groovy-all:2.4.12'
                testCompile 'org.spockframework:spock-core:1.1-groovy-2.4'
                testCompile 'org.gebish:geb-spock:1.1.1'
            }
        '''
        new File(testProjectDir.newFolder('src', 'test', 'groovy'), 'AppSpec.groovy') << '''
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

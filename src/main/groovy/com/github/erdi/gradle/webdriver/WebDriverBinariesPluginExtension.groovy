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

import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.JavaForkOptions
import org.ysb33r.grolifant.api.core.OperatingSystem

import javax.inject.Inject
import java.util.regex.Pattern

import static com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata.*

@SuppressWarnings(['AbstractClassWithPublicConstructor'])
abstract class WebDriverBinariesPluginExtension {

    public static final String DRIVER_URLS_CONFIG_URL =
        'https://raw.githubusercontent.com/webdriverextensions/webdriverextensions-maven-plugin-repository/master/repository-3.0.json'

    private final ObjectFactory objectFactory

    private final DriverConfiguration chromedriverConfiguration
    private final DriverConfiguration geckodriverConfiguration
    private final DriverConfiguration edgedriverConfiguration

    private final Action<JavaForkOptions> configureTaskAction = { javaForkOptions ->
        driverConfigurations.each { driverConfiguration ->
            javaForkOptions.jvmArgumentProviders << new DriverBinaryPathCommandLineArgumentProvider(
                driverConfiguration.driverBinaryPropertiesDirectory
            )
        }
    } as Action<JavaForkOptions>

    @Inject
    WebDriverBinariesPluginExtension(
        Project project, Gradle gradle, ObjectFactory objectFactory, ProjectLayout projectLayout, ProviderFactory providerFactory
    ) {
        this.objectFactory = objectFactory

        this.downloadRoot.convention(
            projectLayout.dir(
                providerFactory.provider { gradle.gradleUserHomeDir }
            )
        )
        this.driverUrlsConfiguration.convention(project.resources.text.fromUri(DRIVER_URLS_CONFIG_URL))
        this.fallbackTo32Bit.convention(false)
        this.architectureProperty.backing.convention(OperatingSystem.current().arch)

        chromedriverConfiguration = this.objectFactory.newInstance(DriverConfiguration, CHROMEDRIVER)
        geckodriverConfiguration = this.objectFactory.newInstance(DriverConfiguration, GECKODRIVER)
        edgedriverConfiguration = this.objectFactory.newInstance(DriverConfiguration, EDGEDRIVER)

        driverConfigurations.each {
            it.fallbackTo32Bit.convention(this.fallbackTo32Bit)
            it.downloadRoot.convention(this.downloadRoot)
            it.driverUrlsConfiguration.convention(this.driverUrlsConfiguration)
            it.architectureProperty.set(this.architectureProperty.backing)
        }
    }

    abstract DirectoryProperty getDownloadRoot()
    abstract Property<TextResource> getDriverUrlsConfiguration()
    abstract Property<Boolean> getFallbackTo32Bit()

    @Nested
    abstract ArchitectureProperty getArchitectureProperty()

    void setChromedriver(String configuredVersion) {
        chromedriver = ~Pattern.quote(configuredVersion)
    }

    void setChromedriver(Pattern configuredVersion) {
        chromedriverConfiguration.versionProperty.set(configuredVersion)
    }

    void chromedriver(Action<DriverConfiguration> action) {
        action.execute(chromedriverConfiguration)
    }

    void setGeckodriver(String configuredVersion) {
        geckodriver = ~Pattern.quote(configuredVersion)
    }

    void setGeckodriver(Pattern configuredVersion) {
        geckodriverConfiguration.versionProperty.set(configuredVersion)
    }

    void geckodriver(Action<DriverConfiguration> action) {
        action.execute(geckodriverConfiguration)
    }

    void setEdgedriver(String configuredVersion) {
        edgedriver = ~Pattern.quote(configuredVersion)
    }

    void setEdgedriver(Pattern configuredVersion) {
        edgedriverConfiguration.versionProperty.set(configuredVersion)
    }

    void edgedriver(Action<DriverConfiguration> action) {
        action.execute(edgedriverConfiguration)
    }

    void setArchitecture(String architecture) {
        this.architectureProperty.set(architecture)
    }

    <T extends JavaForkOptions> void configureTask(TaskProvider<T> task) {
        task.configure(configureTaskAction)
    }

    void configureTask(JavaForkOptions task) {
        configureTaskAction.execute(task)
    }

    <T extends JavaForkOptions> void configureTasks(DomainObjectCollection<T> tasks) {
        tasks.configureEach(configureTaskAction)
    }

    protected List<DriverConfiguration> getDriverConfigurations() {
        [chromedriverConfiguration, geckodriverConfiguration, edgedriverConfiguration]
    }

}

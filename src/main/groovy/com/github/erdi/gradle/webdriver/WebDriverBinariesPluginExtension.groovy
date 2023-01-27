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

import com.github.erdi.gradle.webdriver.task.ConfigureBinary
import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Nested
import org.gradle.process.JavaForkOptions

import javax.inject.Inject
import java.util.regex.Pattern

@SuppressWarnings(['AbstractClassWithPublicConstructor'])
abstract class WebDriverBinariesPluginExtension {

    public static final String DRIVER_URLS_CONFIG_URL =
        'https://raw.githubusercontent.com/webdriverextensions/webdriverextensions-maven-plugin-repository/master/repository-3.0.json'

    private final Project project
    private final ObjectFactory objectFactory

    @Inject
    WebDriverBinariesPluginExtension(Project project) {
        this.project = project

        this.objectFactory = project.objects

        this.downloadRoot.convention(
            project.layout.dir(
                project.providers.provider { project.gradle.gradleUserHomeDir }
            )
        )
        this.driverUrlsConfiguration.convention(project.resources.text.fromUri(DRIVER_URLS_CONFIG_URL))
        this.fallbackTo32Bit.convention(false)

        [chromedriverConfiguration, geckodriverConfiguration, edgedriverConfiguration].each {
            it.fallbackTo32Bit.convention(this.fallbackTo32Bit)
        }
    }

    abstract DirectoryProperty getDownloadRoot()
    abstract Property<TextResource> getDriverUrlsConfiguration()
    abstract Property<Boolean> getFallbackTo32Bit()

    @Nested
    abstract DriverConfiguration getChromedriverConfiguration()

    @Nested
    abstract DriverConfiguration getGeckodriverConfiguration()

    @Nested
    abstract DriverConfiguration getEdgedriverConfiguration()

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

    public <T extends Task & JavaForkOptions> void configureTask(T task) {
        def tasks = objectFactory.domainObjectSet(Task) as DomainObjectCollection<T>
        tasks.add(task)
        configureTasks(tasks)
    }

    public <T extends Task & JavaForkOptions> void configureTasks(DomainObjectCollection<T> tasks) {
        configure(tasks)
    }

    private <T extends Task & JavaForkOptions> void configure(DomainObjectCollection<T> tasks) {
        def configureBinaryTasks = project.tasks.withType(ConfigureBinary)
        tasks.configureEach { T javaForkOptions ->
            javaForkOptions.dependsOn(configureBinaryTasks)
        }
        configureBinaryTasks.configureEach { ConfigureBinary configureBinary ->
            configureBinary.addBinaryAware(new BinaryAwareJavaForkOptions(tasks, configureBinary.webDriverBinaryMetadata.systemProperty))
        }
    }

}

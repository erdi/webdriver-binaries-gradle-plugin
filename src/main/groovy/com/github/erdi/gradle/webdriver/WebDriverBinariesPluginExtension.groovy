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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.resources.TextResource
import org.gradle.process.JavaForkOptions

class WebDriverBinariesPluginExtension {

    public static final String DRIVER_URLS_CONFIG_URL =
        'https://raw.githubusercontent.com/webdriverextensions/webdriverextensions-maven-plugin-repository/master/repository-3.0.json'

    private final Project project
    private final Property<File> downloadRoot
    private final Property<TextResource> driverUrlsConfiguration
    private final Property<Boolean> fallbackTo32Bit

    final DriverConfiguration ieDriverServerConfiguration
    final DriverConfiguration chromedriverConfiguration
    final DriverConfiguration geckodriverConfiguration

    WebDriverBinariesPluginExtension(Project project) {
        this.project = project

        def objectFactory = project.objects
        this.driverUrlsConfiguration = objectFactory.property(TextResource)
        this.downloadRoot = objectFactory.property(File)
        this.fallbackTo32Bit = objectFactory.property(Boolean)
        this.ieDriverServerConfiguration = new DriverConfiguration(project, this.fallbackTo32Bit)
        this.chromedriverConfiguration = new DriverConfiguration(project, this.fallbackTo32Bit)
        this.geckodriverConfiguration = new DriverConfiguration(project, this.fallbackTo32Bit)

        this.driverUrlsConfiguration.set(project.resources.text.fromUri(DRIVER_URLS_CONFIG_URL))
        this.fallbackTo32Bit.set(false)
    }

    void iedriverserver(String configuredVersion) {
        iedriverserver {
            version = configuredVersion
        }
    }

    void setIedriverserver(String configuredVersion) {
        iedriverserver(configuredVersion)
    }

    void iedriverserver(@DelegatesTo(DriverConfiguration) Closure configuration) {
        project.configure(ieDriverServerConfiguration, configuration)
    }

    void chromedriver(String configuredVersion) {
        chromedriver {
            version = configuredVersion
        }
    }

    void setChromedriver(String configuredVersion) {
        chromedriver(configuredVersion)
    }

    void chromedriver(@DelegatesTo(DriverConfiguration) Closure configuration) {
        project.configure(chromedriverConfiguration, configuration)
    }

    void geckodriver(String configuredVersion) {
        geckodriver {
            version = configuredVersion
        }
    }

    void setGeckodriver(String configuredVersion) {
        geckodriver(configuredVersion)
    }

    void geckodriver(@DelegatesTo(DriverConfiguration) Closure configuration) {
        project.configure(geckodriverConfiguration, configuration)
    }

    void setDownloadRoot(File downloadRoot) {
        this.downloadRoot.set(downloadRoot)
    }

    File getDownloadRoot() {
        downloadRoot.get()
    }

    Provider<File> getDownloadRootProvider() {
        downloadRoot
    }

    void setDriverUrlsConfiguration(TextResource driverUrlsConfiguration) {
        this.driverUrlsConfiguration.set(driverUrlsConfiguration)
    }

    TextResource getDriverUrlsConfiguration() {
        driverUrlsConfiguration.get()
    }

    Provider<TextResource> getDriverUrlsConfigurationProvider() {
        driverUrlsConfiguration
    }

    public <T extends Task & JavaForkOptions> void configureTask(T task) {
        configure(task.&each)
    }

    public <T extends Task & JavaForkOptions> void configureTasks(DomainObjectCollection<T> tasks) {
        configure(tasks.&all)
    }

    private <T extends Task & JavaForkOptions> void configure(Action<Action<T>> taskConfigurationAction) {
        project.tasks.withType(ConfigureBinary) { ConfigureBinary configureBinaryTask ->
            taskConfigurationAction.execute { T javaForkOptions ->
                javaForkOptions.dependsOn(configureBinaryTask)
                configureBinaryTask.addBinaryAware(new BinaryAwareJavaForkOptions(javaForkOptions, configureBinaryTask.webDriverBinaryMetadata.systemProperty))
            }
        }
    }

    void setFallbackTo32Bit(boolean fallbackTo32Bit) {
        this.fallbackTo32Bit.set(fallbackTo32Bit)
    }
}

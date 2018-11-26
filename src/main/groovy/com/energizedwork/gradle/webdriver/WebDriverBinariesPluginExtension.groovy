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

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.resources.TextResource

class WebDriverBinariesPluginExtension {

    public static final String DRIVER_URLS_CONFIG_URL =
        'https://raw.githubusercontent.com/webdriverextensions/webdriverextensions-maven-plugin-repository/master/repository-3.0.json'

    private final Project project
    private final Property<File> downloadRoot
    private final Property<TextResource> driverUrlsConfiguration

    final DriverConfiguration ieDriverServerConfiguration
    final DriverConfiguration chromedriverConfiguration
    final DriverConfiguration geckodriverConfiguration

    WebDriverBinariesPluginExtension(Project project) {
        this.project = project

        def objectFactory = project.objects
        this.driverUrlsConfiguration = objectFactory.property(TextResource)
        this.downloadRoot = objectFactory.property(File)
        this.ieDriverServerConfiguration = new DriverConfiguration(project)
        this.chromedriverConfiguration = new DriverConfiguration(project)
        this.geckodriverConfiguration = new DriverConfiguration(project)

        this.driverUrlsConfiguration.set(project.resources.text.fromUri(DRIVER_URLS_CONFIG_URL))
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
}

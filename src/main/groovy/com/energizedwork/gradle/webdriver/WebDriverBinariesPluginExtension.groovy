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

class WebDriverBinariesPluginExtension {

    private final Project project
    private final Property<File> downloadRoot
    private final Property<String> chromedriver
    private final Property<String> geckodriver

    final IeDriverServerConfiguration ieDriverServerConfiguration

    WebDriverBinariesPluginExtension(Project project) {
        this.project = project

        def objectFactory = project.objects
        this.downloadRoot = objectFactory.property(File)
        this.chromedriver = objectFactory.property(String)
        this.geckodriver = objectFactory.property(String)
        this.ieDriverServerConfiguration = new IeDriverServerConfiguration(project)
    }

    void iedriverserver(String configuredVersion) {
        iedriverserver {
            version = configuredVersion
        }
    }

    void setIedriverserver(String configuredVersion) {
        iedriverserver {
            version = configuredVersion
        }
    }

    void iedriverserver(@DelegatesTo(IeDriverServerConfiguration) Closure configuration) {
        project.configure(ieDriverServerConfiguration, configuration)
    }

    void getChromedriver() {
        chromedriver.get()
    }

    void setChromedriver(String version) {
        chromedriver.set(version)
    }

    Provider<String> getChromedriverProvider() {
        chromedriver
    }

    void getGeckodriver() {
        geckodriver.get()
    }

    void setGeckodriver(String version) {
        geckodriver.set(version)
    }

    Provider<String> getGeckodriverProvider() {
        geckodriver
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
}

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
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider

class WebDriverBinariesPluginExtension {

    private final Project project

    PropertyState<File> downloadRoot
    PropertyState<String> chromedriver
    PropertyState<String> geckodriver

    WebDriverBinariesPluginExtension(Project project) {
        this.project = project
        this.downloadRoot = project.property(File)
        this.chromedriver = project.property(String)
        this.geckodriver = project.property(String)
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

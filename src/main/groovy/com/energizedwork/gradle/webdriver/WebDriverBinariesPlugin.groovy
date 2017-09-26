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

import com.energizedwork.gradle.webdriver.chrome.ConfigureChromeDriverBinary
import com.energizedwork.gradle.webdriver.gecko.ConfigureGeckoDriverBinary
import com.energizedwork.gradle.webdriver.task.ConfigureBinary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.ide.idea.GenerateIdeaWorkspace

class WebDriverBinariesPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = 'webdriverBinaries'

    private static final String CHROMEDRIVER_PATH_SYSTEM_PROPERTY = 'webdriver.chrome.driver'
    private static final String GECKODRIVER_PATH_SYSTEM_PROPERTY = 'webdriver.gecko.driver'
    private static final String IDEA_JUNIT_PLUGIN_ID = 'com.energizedwork.idea-junit'
    private static final String IDEA_JUNIT_EXTENSION_NAME = 'ideaJunit'

    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, WebDriverBinariesPluginExtension, project)
        createConfigureChromeDriverBinary(project, extension)
        createConfigureGeckoDriverBinary(project, extension)
    }

    ConfigureChromeDriverBinary createConfigureChromeDriverBinary(Project project, WebDriverBinariesPluginExtension extension) {
        createConfigureDriverBinary(ConfigureChromeDriverBinary, project, extension, extension.chromedriverProvider, CHROMEDRIVER_PATH_SYSTEM_PROPERTY)
    }

    ConfigureGeckoDriverBinary createConfigureGeckoDriverBinary(Project project, WebDriverBinariesPluginExtension extension) {
        createConfigureDriverBinary(ConfigureGeckoDriverBinary, project, extension, extension.geckodriverProvider, GECKODRIVER_PATH_SYSTEM_PROPERTY)
    }

    private <T extends ConfigureBinary> T createConfigureDriverBinary(Class<T> taskType, Project project, WebDriverBinariesPluginExtension extension,
                                                                      Provider<String> versionProvider, String systemPropertyName) {
        T configureTask = project.task(taskType.simpleName.uncapitalize(), type: taskType)
        configureTask.downloadRoot = extension.downloadRootProvider
        configureTask.version = versionProvider
        configureTestTasksWithWebDriverBinary(project, configureTask, systemPropertyName)
        configureIdeaWithWebDriverBinary(project, configureTask, systemPropertyName)
        configureTask
    }

    private void configureTestTasksWithWebDriverBinary(Project project, Task configureTask, String systemPropertyName) {
        project.tasks.withType(Test) { Test test ->
            test.dependsOn(configureTask)
            configureTask.addBinaryAware(new BinaryAwareTest(test, systemPropertyName))
        }
    }

    private void configureIdeaWithWebDriverBinary(Project project, Task configureTask, String systemPropertyName) {
        project.pluginManager.withPlugin(IDEA_JUNIT_PLUGIN_ID) {
            project.tasks.withType(GenerateIdeaWorkspace) { ideaWorkspace ->
                ideaWorkspace.dependsOn(configureTask)
            }
            project.extensions.configure(IDEA_JUNIT_EXTENSION_NAME) { extension ->
                configureTask.addBinaryAware(new BinaryAwareProperties(extension.systemProperties, systemPropertyName))
            }
        }
    }

}

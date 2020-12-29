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

import com.github.erdi.gradle.webdriver.chrome.ConfigureChromeDriverBinary
import com.github.erdi.gradle.webdriver.edge.ConfigureEdgeDriverBinary
import com.github.erdi.gradle.webdriver.gecko.ConfigureGeckoDriverBinary
import com.github.erdi.gradle.webdriver.ie.ConfigureIeDriverServerBinary
import com.github.erdi.gradle.webdriver.task.ConfigureBinary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.ide.idea.GenerateIdeaWorkspace
import org.gradle.plugins.ide.idea.model.IdeaModel

class WebDriverBinariesPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = 'webdriverBinaries'

    private static final String EXTENDED_IDEA_PLUGIN_ID = 'com.github.erdi.extended-idea'
    private static final String EXTENDED_IDEA_EXTENSION_NAME = 'extended'

    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, WebDriverBinariesPluginExtension, project)
        extension.configureTasks(project.tasks.withType(Test))
        createConfigureChromeDriverBinary(project, extension)
        createConfigureGeckoDriverBinary(project, extension)
        createConfigureInternetExplorerDriverServerBinary(project, extension)
        createConfigureEdgeDriverBinary(project, extension)
    }

    ConfigureChromeDriverBinary createConfigureChromeDriverBinary(Project project, WebDriverBinariesPluginExtension extension) {
        createConfigureDriverBinary(ConfigureChromeDriverBinary, project, extension, extension.chromedriverConfiguration)
    }

    ConfigureGeckoDriverBinary createConfigureGeckoDriverBinary(Project project, WebDriverBinariesPluginExtension extension) {
        createConfigureDriverBinary(ConfigureGeckoDriverBinary, project, extension, extension.geckodriverConfiguration)
    }

    ConfigureIeDriverServerBinary createConfigureInternetExplorerDriverServerBinary(Project project, WebDriverBinariesPluginExtension extension) {
        createConfigureDriverBinary(ConfigureIeDriverServerBinary, project, extension, extension.ieDriverServerConfiguration)
    }

    ConfigureEdgeDriverBinary createConfigureEdgeDriverBinary(Project project, WebDriverBinariesPluginExtension extension) {
        createConfigureDriverBinary(ConfigureEdgeDriverBinary, project, extension, extension.edgedriverConfiguration)
    }

    private <T extends ConfigureBinary> T createConfigureDriverBinary(Class<T> taskType, Project project, WebDriverBinariesPluginExtension extension,
                                                                      DriverConfiguration driverConfiguration) {
        T configureTask = project.task(taskType.simpleName.uncapitalize(), type: taskType)
        configureTask.downloadRoot = extension.downloadRootProvider
        configureTask.driverUrlsConfiguration = extension.driverUrlsConfigurationProvider
        configureTask.version = driverConfiguration.versionProvider
        configureTask.architecture = driverConfiguration.architectureProvider
        configureTask.fallbackTo32Bit = driverConfiguration.fallbackTo32BitProvider
        configureIdeaWithWebDriverBinary(project, configureTask)
        configureTask
    }

    private void configureIdeaWithWebDriverBinary(Project project, ConfigureBinary configureTask) {
        project.pluginManager.withPlugin(EXTENDED_IDEA_PLUGIN_ID) {
            project.tasks.withType(GenerateIdeaWorkspace) { GenerateIdeaWorkspace ideaWorkspace ->
                ideaWorkspace.dependsOn(configureTask)
            }
            project.extensions.configure(IdeaModel) { ideaModel ->
                def jUnitSystemProperties = (ideaModel as ExtensionAware).extensions.getByName(EXTENDED_IDEA_EXTENSION_NAME).workspace.junit.systemProperties
                configureTask.addBinaryAware(new BinaryAwareProperties(jUnitSystemProperties, configureTask.webDriverBinaryMetadata.systemProperty))
            }
        }
    }

}

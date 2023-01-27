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
import org.gradle.api.tasks.TaskProvider
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
        registerConfigureChromeDriverBinary(project, extension)
        registerConfigureGeckoDriverBinary(project, extension)
        registerConfigureInternetExplorerDriverServerBinary(project, extension)
        registerConfigureEdgeDriverBinary(project, extension)
    }

    private void registerConfigureChromeDriverBinary(Project project, WebDriverBinariesPluginExtension extension) {
        registerConfigureDriverBinary(ConfigureChromeDriverBinary, project, extension, extension.chromedriverConfiguration)
    }

    private void registerConfigureGeckoDriverBinary(Project project, WebDriverBinariesPluginExtension extension) {
        registerConfigureDriverBinary(ConfigureGeckoDriverBinary, project, extension, extension.geckodriverConfiguration)
    }

    private void registerConfigureInternetExplorerDriverServerBinary(Project project, WebDriverBinariesPluginExtension extension) {
        registerConfigureDriverBinary(ConfigureIeDriverServerBinary, project, extension, extension.ieDriverServerConfiguration)
    }

    private void registerConfigureEdgeDriverBinary(Project project, WebDriverBinariesPluginExtension extension) {
        registerConfigureDriverBinary(ConfigureEdgeDriverBinary, project, extension, extension.edgedriverConfiguration)
    }

    private <T extends ConfigureBinary> TaskProvider<T> registerConfigureDriverBinary(
        Class<T> taskType, Project project, WebDriverBinariesPluginExtension extension, DriverConfiguration driverConfiguration
    ) {
        def configureTask = project.tasks.register(taskType.simpleName.uncapitalize(), taskType) {
            downloadRoot.fileProvider(extension.downloadRootProvider)
            driverUrlsConfiguration.convention(extension.driverUrlsConfigurationProvider)
            version.convention(driverConfiguration.versionProvider)
            architecture.convention(driverConfiguration.architectureProvider)
            fallbackTo32Bit.convention(driverConfiguration.fallbackTo32BitProvider)
        }
        configureIdeaWithWebDriverBinary(project, configureTask)
        configureTask
    }

    private void configureIdeaWithWebDriverBinary(Project project, TaskProvider<ConfigureBinary> configureTask) {
        project.pluginManager.withPlugin(EXTENDED_IDEA_PLUGIN_ID) {
            project.tasks.withType(GenerateIdeaWorkspace).configureEach { GenerateIdeaWorkspace ideaWorkspace ->
                ideaWorkspace.dependsOn(configureTask)
            }
            project.extensions.configure(IdeaModel) { ideaModel ->
                configureTask.configure {
                    def extendedIdeaExtension = (ideaModel as ExtensionAware).extensions.getByName(EXTENDED_IDEA_EXTENSION_NAME)
                    def jUnitSystemProperties = extendedIdeaExtension.workspace.junit.systemProperties
                    it.addBinaryAware(new BinaryAwareProperties(jUnitSystemProperties, it.webDriverBinaryMetadata.systemProperty))
                }
            }
        }
    }

}

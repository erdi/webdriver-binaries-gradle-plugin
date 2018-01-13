/*
 * Copyright 2018 the original author or authors.
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
package com.energizedwork.gradle.webdriver.ie

import org.gradle.api.Project
import org.ysb33r.grolifant.api.AbstractDistributionInstaller
import org.ysb33r.grolifant.api.OperatingSystem

class InternetExplorerDriverServerDistributionInstaller extends AbstractDistributionInstaller {

    InternetExplorerDriverServerDistributionInstaller(Project project, File downloadRoot, String distributionVersion) {
        super('InternetExplorerDriverServer', distributionVersion, "webdriver/iedriverserver/$distributionVersion", project)
        this.downloadRoot = downloadRoot
    }

    @Override
    URI uriFromVersion(String version) {
        "http://selenium-release.storage.googleapis.com/${stripMinorVersion(version)}/IEDriverServer_${archPart}_${version}.zip".toURI()
    }

    @Override
    protected File getAndVerifyDistributionRoot(File distDir, String distributionDescription) {
        distDir
    }

    private String stripMinorVersion(String version) {
        version[0..<version.lastIndexOf('.')]
    }

    private getArchPart() {
        def arch = OperatingSystem.current().arch
        switch (arch) {
            case OperatingSystem.Arch.X86_64:
                return 'x64'
            case OperatingSystem.Arch.X86:
                return 'Win32'
            default:
                throw new IllegalStateException("Unsupported architecture: $arch")
        }
    }

}

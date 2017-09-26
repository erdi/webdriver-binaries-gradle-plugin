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
package com.energizedwork.gradle.webdriver.gecko

import org.gradle.api.Project
import org.ysb33r.gradle.olifant.AbstractDistributionInstaller
import org.ysb33r.gradle.olifant.OperatingSystem
import org.ysb33r.gradle.olifant.internal.os.MacOsX
import org.ysb33r.gradle.olifant.internal.os.Windows

class GeckoDriverDistributionInstaller extends AbstractDistributionInstaller {

    private final OperatingSystem os

    GeckoDriverDistributionInstaller(Project project, File downloadRoot, String distributionVersion, OperatingSystem os) {
        super('GeckoDriver', distributionVersion, "webdriver/geckodriver/$distributionVersion", project)
        this.downloadRoot = downloadRoot
        this.os = os
    }

    @Override
    URI uriFromVersion(String version) {
        "https://github.com/mozilla/geckodriver/releases/download/v$version/geckodriver-v$version-${getOsUriPart(version)}.$archiveExtension".toURI()
    }

    @Override
    protected File getAndVerifyDistributionRoot(File distDir, String distributionDescription) {
        distDir
    }

    private String getArchiveExtension() {
        (os in Windows) ? 'zip' : 'tar.gz'
    }

    private String getOsUriPart(String version) {
        switch (os) {
            case MacOsX:
                return version == '0.9.0' ? 'mac' : 'macos'
            case Windows:
                return 'win64'
            default:
                return 'linux64'
        }
    }
}

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
package com.energizedwork.gradle.webdriver.chrome

import org.gradle.api.Project
import org.ysb33r.gradle.olifant.AbstractDistributionInstaller
import org.ysb33r.gradle.olifant.OperatingSystem
import org.ysb33r.gradle.olifant.internal.os.MacOsX
import org.ysb33r.gradle.olifant.internal.os.Windows

import static org.ysb33r.gradle.olifant.OperatingSystem.Arch.X86_64

class ChromeDriverDistributionInstaller extends AbstractDistributionInstaller {

    private final static List<String> MAC_32_BIT_ONLY_VERSIONS = (0..22).collect { "2.$it".toString() }

    private final OperatingSystem os

    ChromeDriverDistributionInstaller(Project project, File downloadRoot, String distributionVersion, OperatingSystem os) {
        super('ChromeDriver', distributionVersion, "webdriver/chromedriver/$distributionVersion", project)
        this.downloadRoot = downloadRoot
        this.os = os
    }

    @Override
    URI uriFromVersion(String version) {
        "https://chromedriver.storage.googleapis.com/$version/chromedriver_$osUriPart${architectureUriPart(version)}.zip".toURI()
    }

    @Override
    protected File getAndVerifyDistributionRoot(File distDir, String distributionDescription) {
        distDir
    }

    private String getOsUriPart() {
        switch (os) {
            case MacOsX:
                return 'mac'
            case Windows:
                return 'win'
            default:
                return 'linux'
        }
    }

    private String architectureUriPart(String version) {
        switch (os) {
            case MacOsX:
                return (version in MAC_32_BIT_ONLY_VERSIONS) ? '32' : '64'
            case Windows:
                return '32'
            default:
                return (os.arch == X86_64) ? '64' : '32'
        }
    }
}

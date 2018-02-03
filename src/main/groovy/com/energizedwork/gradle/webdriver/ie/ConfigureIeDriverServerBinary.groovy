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
package com.energizedwork.gradle.webdriver.ie

import com.energizedwork.gradle.webdriver.task.ConfigureBinary
import org.gradle.api.tasks.Internal
import org.ysb33r.grolifant.api.AbstractDistributionInstaller
import org.ysb33r.grolifant.api.OperatingSystem
import org.ysb33r.grolifant.api.OperatingSystem.Arch
import org.ysb33r.grolifant.api.os.Windows

class ConfigureIeDriverServerBinary extends ConfigureBinary {

    final String binaryName = 'IEDriverServer'

    @Internal
    Arch architecture

    @Override
    @SuppressWarnings('UnnecessaryGetter')
    protected AbstractDistributionInstaller distributionInstaller() {
        new InternetExplorerDriverServerDistributionInstaller(project, getDownloadRoot(), getVersion(), getArchitecture())
    }

    @Override
    protected OperatingSystem getOperatingSystem() {
        Windows.INSTANCE
    }

}

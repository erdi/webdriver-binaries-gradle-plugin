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
package com.energizedwork.gradle.webdriver

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant.api.OperatingSystem

class IeDriverServerConfiguration {

    private final Property<String> version
    private final Property<OperatingSystem.Arch> architecture

    IeDriverServerConfiguration(Project project) {
        def objectFactory = project.objects
        this.version = objectFactory.property(String)
        this.architecture = objectFactory.property(OperatingSystem.Arch)
        this.architecture.set(OperatingSystem.current().arch)
    }

    void setArchitecture(String architecture) {
        if (!(architecture in ['X86', 'X86_64'])) {
            throw new IllegalArgumentException("Unsupported architecture value '$architecture'. Supported values are 'X86' and 'X86_64'.")
        }
        this.architecture.set(OperatingSystem.Arch.valueOf(architecture))
    }

    OperatingSystem.Arch getArchitecture() {
        architecture.get()
    }

    String getVersion() {
        version.get()
    }

    void setVersion(String version) {
        this.version.set(version)
    }

    Provider<String> getVersionProvider() {
        version
    }
}

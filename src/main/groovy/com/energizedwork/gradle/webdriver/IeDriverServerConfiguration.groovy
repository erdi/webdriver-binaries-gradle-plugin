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

import org.ysb33r.grolifant.api.OperatingSystem

class IeDriverServerConfiguration {

    String version
    private OperatingSystem.Arch architecture

    void setArchitecture(String architecture) {
        if (!(architecture in ['X86', 'X86_64'])) {
            throw new IllegalArgumentException("Unsupported architecture value '$architecture'. Supported values are 'X86' and 'X86_64'.")
        }
        this.architecture = OperatingSystem.Arch.valueOf(architecture)
    }

    OperatingSystem.Arch getArchitecture() {
        architecture ?: OperatingSystem.current().arch
    }

}

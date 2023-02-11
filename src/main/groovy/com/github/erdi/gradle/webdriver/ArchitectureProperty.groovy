/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.erdi.gradle.webdriver

import com.github.erdi.gradle.webdriver.repository.DriverUrlsConfiguration
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant.api.core.OperatingSystem

abstract class ArchitectureProperty {

    abstract Property<OperatingSystem.Arch> getBacking()

    void set(String architecture) {
        def supportedArchitectures = DriverUrlsConfiguration.BITS.keySet()*.toString()
        if (!(architecture in supportedArchitectures)) {
            def quotedArchitectures = supportedArchitectures.collect { "'${it}'" }
            def supportedArchitecturesString = [quotedArchitectures[0..-2].join(', '), quotedArchitectures.last()].join(' and ')
            throw new IllegalArgumentException("Unsupported architecture value '$architecture'. Supported values are ${supportedArchitecturesString}.")
        }
        backing.set(OperatingSystem.Arch.valueOf(architecture))
    }

    void set(Provider<? extends OperatingSystem.Arch> architecture) {
        backing.set(architecture)
    }

}

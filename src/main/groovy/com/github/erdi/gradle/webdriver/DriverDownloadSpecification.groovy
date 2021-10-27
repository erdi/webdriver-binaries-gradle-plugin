/*
 * Copyright 2019 the original author or authors.
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

import groovy.transform.builder.Builder
import org.ysb33r.grolifant.api.core.OperatingSystem

class DriverDownloadSpecification {

    final String name
    final String version
    final OperatingSystem os
    final OperatingSystem.Arch arch
    final boolean fallbackTo32Bit

    @Builder
    DriverDownloadSpecification(String name, String version, OperatingSystem os, OperatingSystem.Arch arch, boolean fallbackTo32Bit) {
        this.name = name
        this.version = version
        this.os = os
        this.arch = arch
        this.fallbackTo32Bit = fallbackTo32Bit
    }

}

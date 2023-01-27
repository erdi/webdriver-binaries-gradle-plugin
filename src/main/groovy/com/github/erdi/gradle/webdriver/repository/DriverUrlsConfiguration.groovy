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
package com.github.erdi.gradle.webdriver.repository

import com.github.erdi.gradle.webdriver.DriverDownloadSpecification
import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.maven.artifact.versioning.ComparableVersion
import org.ysb33r.grolifant.api.core.OperatingSystem
import org.ysb33r.grolifant.api.core.os.Linux
import org.ysb33r.grolifant.api.core.os.MacOsX
import org.ysb33r.grolifant.api.core.os.Windows

import static org.ysb33r.grolifant.api.core.OperatingSystem.Arch.ARM64
import static org.ysb33r.grolifant.api.core.OperatingSystem.Arch.X86
import static org.ysb33r.grolifant.api.core.OperatingSystem.Arch.X86_64

class DriverUrlsConfiguration {

    public static final Map<OperatingSystem, String> PLATFORMS = [(Linux.INSTANCE): 'linux', (Windows.INSTANCE): 'windows', (MacOsX.INSTANCE): 'mac']

    @SuppressWarnings('DuplicateStringLiteral')
    public static final Map<OperatingSystem.Arch, String> BITS = [(X86): '32', (X86_64): '64', (ARM64): '64']
    public static final Map<OperatingSystem.Arch, String> ARCHS = [(X86): 'x86', (X86_64): 'amd64', (ARM64): 'aarch64']

    private final List<Map> drivers

    DriverUrlsConfiguration(File file) {
        def configuration
        try {
            configuration = new JsonSlurper().parse(file)
        } catch (JsonException e) {
            throw new InvalidDriverUrlsConfigurationException(e)
        }
        validate(configuration)
        drivers = configuration.drivers
    }

    VersionAndUri versionAndUriFor(DriverDownloadSpecification spec) {
        def architectures = [spec.arch] as Set
        if (spec.fallbackTo32Bit && architectures.contains(X86_64)) {
            architectures << X86
        }
        def platform = platform(spec.os)
        def bits = architectures.collect { bit(it) } as Set
        def archs = architectures.collect { arch(it) } as Set

        def matchingDrivers = drivers.findAll {
            it.name == spec.name &&
                it.platform == platform &&
                spec.version.matcher(it.version.toString()).matches() &&
                bits.contains(it.bit) &&
                ((it.arch == null && architectures.intersect([X86, X86_64])) || archs.contains(it.arch))
        }.sort { left, right ->
            def leftComparableVersion = new ComparableVersion(left.version.toString())
            def rightComparableVersion = new ComparableVersion(right.version.toString())

            rightComparableVersion <=> leftComparableVersion ?: (right.bit <=> left.bit)
        }

        if (!matchingDrivers) {
            throw DriverUrlNotFoundException.builder()
                .name(spec.name)
                .version(spec.version)
                .platform(platform)
                .bits(bits)
                .archs(archs)
                .build()
        }

        def driver = matchingDrivers.first()
        new VersionAndUri(driver.version.toString(), new URI(driver.url.toString()))
    }

    private void validate(Object configuration) {
        if (!(configuration instanceof Map)) {
            throw new InvalidDriverUrlsConfigurationException('Driver urls configuration file should contain a json object')
        }
        if (!configuration.containsKey('drivers')) {
            throw new InvalidDriverUrlsConfigurationException('''Driver urls configuration file should contain a json object with 'drivers' key''')
        }
        if (!(configuration.drivers instanceof List)) {
            throw new InvalidDriverUrlsConfigurationException('\'drivers\' key in driver urls configuration file should contain a list')
        }
        List drivers = configuration.drivers
        if (drivers.any { !(it instanceof Map) }) {
            throw new InvalidDriverUrlsConfigurationException('Each driver entry in driver urls configuration file should be a json object')
        }
        List<Map> driverObjects = drivers
        if (driverObjects.any { !it.containsKey('url') }) {
            throw new InvalidDriverUrlsConfigurationException('''Each driver entry in driver urls configuration file should contain 'url' key''')
        }
        if (driverObjects.any { !(it.url instanceof String) }) {
            def message = '''Each driver entry in driver urls configuration file should contain a 'url' key with a string value'''
            throw new InvalidDriverUrlsConfigurationException(message)
        }
    }

    private String arch(OperatingSystem.Arch arch) {
        Optional.ofNullable(ARCHS[arch])
            .orElseThrow { new UnsupportedArchitectureException(arch) }
    }

    private String bit(OperatingSystem.Arch arch) {
        Optional.ofNullable(BITS[arch])
            .orElseThrow { new UnsupportedArchitectureException(arch) }
    }

    private String platform(OperatingSystem operatingSystem) {
        Optional.ofNullable(PLATFORMS[operatingSystem])
            .orElseThrow { new UnsupportedOperatingSystemException(operatingSystem) }
    }

}

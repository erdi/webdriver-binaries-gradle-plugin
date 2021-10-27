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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ysb33r.grolifant.api.core.OperatingSystem
import org.ysb33r.grolifant.api.core.os.*
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

import static groovy.json.JsonOutput.toJson
import static org.ysb33r.grolifant.api.core.OperatingSystem.Arch.X86
import static org.ysb33r.grolifant.api.core.OperatingSystem.Arch.X86_64

class DriverUrlsConfigurationSpec extends Specification {

    private static final List<String> DRIVER_NAMES = ['chromedriver', 'geckodriver', 'internetexplorerdriver', 'edgedriver']
    private static final List<String> DRIVER_VERSIONS = ['0.22.0', '2.42.0']

    @Rule
    TemporaryFolder temporaryFolder

    File configurationFile

    void setup() {
        configurationFile = temporaryFolder.newFile('repository.json')
    }

    def 'an exception is raised when configuration file is not valid json'() {
        given:
        configuration '%&^%&^*'

        when:
        parseConfiguration()

        then:
        InvalidDriverUrlsConfigurationException e = thrown()

        and:
        e.cause in JsonException
    }

    def 'an exception is raised when configuration file contains a json list'() {
        given:
        configuration([])

        when:
        parseConfiguration()

        then:
        InvalidDriverUrlsConfigurationException e = thrown()

        and:
        e.message == 'Driver urls configuration file should contain a json object'
    }

    def 'an exception is raised when configuration file does not contain a top level drivers key'() {
        given:
        configuration([:])

        when:
        parseConfiguration()

        then:
        InvalidDriverUrlsConfigurationException e = thrown()

        and:
        e.message == '''Driver urls configuration file should contain a json object with 'drivers' key'''
    }

    def 'an exception is raised when configuration top level drivers key does not contain a list'() {
        given:
        configuration(drivers: [:])

        when:
        parseConfiguration()

        then:
        InvalidDriverUrlsConfigurationException e = thrown()

        and:
        e.message == '\'drivers\' key in driver urls configuration file should contain a list'
    }

    def 'an exception is raised when not all driver entries are json objects'() {
        given:
        configuration(drivers: ['not an object'])

        when:
        parseConfiguration()

        then:
        InvalidDriverUrlsConfigurationException e = thrown()

        and:
        e.message == 'Each driver entry in driver urls configuration file should be a json object'
    }

    def 'an exception is raised when not all driver entries contain a url key'() {
        given:
        configuration(drivers: [[:]])

        when:
        parseConfiguration()

        then:
        InvalidDriverUrlsConfigurationException e = thrown()

        and:
        e.message == '''Each driver entry in driver urls configuration file should contain 'url' key'''
    }

    @Unroll
    def 'an exception is raised when not all driver entries contain a non null url key'() {
        given:
        configuration(drivers: [[url: url]])

        when:
        parseConfiguration()

        then:
        InvalidDriverUrlsConfigurationException e = thrown()

        and:
        e.message == 'Each driver entry in driver urls configuration file should contain a \'url\' key with a string value'

        where:
        url << [null, 2]
    }

    @Unroll
    def 'an exception is raised when a url for a driver cannot be found'() {
        given:
        emptyConfiguration()

        when:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(Pattern.quote(version))
                .os(os)
                .arch(arch)
                .build()
        )

        then:
        DriverUrlNotFoundException e = thrown()

        and:
        e.message == /Driver url not found for name: "$name", version regexp: "${Pattern.quote(version)}", platform: "$platform", bit: "$bit"/

        where:
        name                     | version  | os               | platform  | arch   | bit
        'chromedriver'           | '2.42.0' | Linux.INSTANCE   | 'linux'   | X86    | '32'
        'geckodriver'            | '0.22.0' | MacOsX.INSTANCE  | 'mac'     | X86_64 | '64'
        'internetexplorerdriver' | '3.14.0' | Windows.INSTANCE | 'windows' | X86_64 | '64'
    }

    @Unroll
    def 'an exception is raised when a url for a driver cannot be found when falling back to 32 bit version'() {
        given:
        emptyConfiguration()

        when:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(Pattern.quote(version))
                .os(os)
                .arch(arch)
                .fallbackTo32Bit(true)
                .build()
        )

        then:
        DriverUrlNotFoundException e = thrown()

        and:
        e.message == /Driver url not found for name: "$name", version regexp: "${Pattern.quote(version)}", platform: "$platform", bit: "$bit"/

        where:
        name           | version  | os              | platform | arch   | bit
        'chromedriver' | '2.42.0' | Linux.INSTANCE  | 'linux'  | X86    | '32'
        'geckodriver'  | '0.22.0' | MacOsX.INSTANCE | 'mac'    | X86_64 | '64 or 32'
    }

    @Unroll
    def 'an exception is raised when searching for a url for an unsupported operating system'() {
        given:
        emptyConfiguration()

        when:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .os(unsupportedOs)
                .arch(X86)
                .build()
        )

        then:
        UnsupportedOperatingSystemException e = thrown()

        and:
        e.message == unsupportedOs.class.simpleName

        where:
        unsupportedOs << [NetBSD.INSTANCE, FreeBSD.INSTANCE, GenericUnix.INSTANCE]
    }

    @Unroll
    def 'an exception is raised when searching for a url for an unsupported architecture'() {
        given:
        emptyConfiguration()

        when:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .os(Linux.INSTANCE)
                .arch(unsupportedArch)
                .version(Pattern.quote('1.2.3'))
                .build()
        )

        then:
        UnsupportedArchitectureException e = thrown()

        and:
        e.message == unsupportedArch.name()

        where:
        unsupportedArch << (OperatingSystem.Arch.values() - DriverUrlsConfiguration.BITS.keySet())
    }

    @Unroll
    def 'urls are searched based on driver name'() {
        given:
        def drivers = DRIVER_NAMES.collect { name ->
            baseDriverProperties + [name: name, url: "http://${name}.com"]
        }

        and:
        configuration(drivers: drivers)

        expect:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(driverName)
                .version(Pattern.quote(version))
                .os(os)
                .arch(arch)
                .build()
        ).uri.toString() == "http://${driverName}.com"

        where:
        driverName << DRIVER_NAMES
        version = '2.41.0'
        os = Linux.INSTANCE
        arch = X86
        baseDriverProperties = [
            version: version,
            platform: DriverUrlsConfiguration.PLATFORMS[os],
            bit: DriverUrlsConfiguration.BITS[arch]
        ]
    }

    @Unroll
    def 'urls are searched based on driver version'() {
        given:
        def drivers = DRIVER_VERSIONS.collect { version ->
            baseDriverProperties + [version: version, url: "http://${version}.com"]
        }

        and:
        configuration(drivers: drivers)

        expect:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(Pattern.quote(driverVersion))
                .os(os)
                .arch(arch)
                .build()
        ).uri.toString() == "http://${driverVersion}.com"

        where:
        driverVersion << DRIVER_VERSIONS
        name = 'chromedriver'
        os = Linux.INSTANCE
        arch = X86
        baseDriverProperties = [
            name: name,
            platform: DriverUrlsConfiguration.PLATFORMS[os],
            bit: DriverUrlsConfiguration.BITS[arch]
        ]
    }

    @Unroll
    def 'driver version is treated as a regular expression and the configuration with the newest version is used'() {
        given:
        def drivers = ['9.0', '19.0', '9.1'].collect { version ->
            baseDriverProperties + [version: version, url: "http://${version}.com"]
        }

        and:
        configuration(drivers: drivers)

        expect:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(versionString)
                .os(os)
                .arch(arch)
                .build()
        ).uri.toString() == "http://${matchingVersion}.com"

        where:
        versionString | matchingVersion
        '.*'          | '19.0'
        /9\..*/       | '9.1'

        name = 'chromedriver'
        os = Linux.INSTANCE
        arch = X86
        baseDriverProperties = [
            name: name,
            platform: DriverUrlsConfiguration.PLATFORMS[os],
            bit: DriverUrlsConfiguration.BITS[arch]
        ]
    }

    @Unroll
    def 'urls are searched based on operation system'() {
        given:
        def drivers = DriverUrlsConfiguration.PLATFORMS.values().collect { platform ->
            baseDriverProperties + [platform: platform, url: "http://${platform}.com"]
        }

        and:
        configuration(drivers: drivers)

        expect:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(version)
                .os(os)
                .arch(arch)
                .build()
        ).uri.toString() == "http://${DriverUrlsConfiguration.PLATFORMS[os]}.com"

        where:
        os << DriverUrlsConfiguration.PLATFORMS.keySet()
        name = 'chromedriver'
        version = '2.42.0'
        arch = X86
        baseDriverProperties = [
            name: name,
            version: version,
            bit: DriverUrlsConfiguration.BITS[arch]
        ]
    }

    @Unroll
    def 'urls are searched based on architecture'() {
        given:
        def drivers = DriverUrlsConfiguration.BITS.values().collect { bit ->
            baseDriverProperties + [bit: bit, url: "http://${bit}.com"]
        }

        and:
        configuration(drivers: drivers)

        expect:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(Pattern.quote(version))
                .os(os)
                .arch(arch)
                .build()
        ).uri.toString() == "http://${DriverUrlsConfiguration.BITS[arch]}.com"

        where:
        arch << DriverUrlsConfiguration.BITS.keySet()
        name = 'chromedriver'
        version = '2.42.0'
        os = Linux.INSTANCE
        baseDriverProperties = [
            name: name,
            version: version,
            platform: DriverUrlsConfiguration.PLATFORMS[os]
        ]
    }

    def 'urls are searched with fallback to 32 bit'() {
        given:
        configuration(drivers: [baseDriverProperties + [bit: DriverUrlsConfiguration.BITS[X86], url: urlFor32Bit]])

        expect:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(Pattern.quote(version))
                .os(os)
                .arch(X86_64)
                .fallbackTo32Bit(true)
                .build()
        ).uri.toString() == urlFor32Bit

        where:
        name = 'chromedriver'
        version = '2.42.0'
        os = Linux.INSTANCE
        baseDriverProperties = [
            name: name,
            version: version,
            platform: DriverUrlsConfiguration.PLATFORMS[os]
        ]
        urlFor32Bit = 'http://fallback.com'
    }

    def 'the 64 bit urls are used when urls for both 32 and 64 bits are available and searching with fallback to 32 bit'() {
        given:
        def drivers = [
            baseDriverProperties + [bit: DriverUrlsConfiguration.BITS[X86], url: 'http://32bit.com'],
            baseDriverProperties + [bit: DriverUrlsConfiguration.BITS[X86_64], url: urlFor64Bit]
        ]

        and:
        configuration(drivers: drivers)

        expect:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(Pattern.quote(version))
                .os(os)
                .arch(X86_64)
                .fallbackTo32Bit(true)
                .build()
        ).uri.toString() == urlFor64Bit

        where:
        name = 'chromedriver'
        version = '2.42.0'
        os = Linux.INSTANCE
        baseDriverProperties = [
            name: name,
            version: version,
            platform: DriverUrlsConfiguration.PLATFORMS[os]
        ]
        urlFor64Bit = 'http://64bit.com'
    }

    def 'urls for 32 bit are ignored when searching for 64 bit urls and fallback to 32 bit is not enabled'() {
        given:
        configuration(drivers: [baseDriverProperties + [bit: DriverUrlsConfiguration.BITS[X86], url: 'http://32bit.com']])

        when:
        parseConfiguration().versionAndUriFor(
            DriverDownloadSpecification.builder()
                .name(name)
                .version(Pattern.quote(version))
                .os(os)
                .arch(arch)
                .build()
        ).uri.toString()

        then:
        DriverUrlNotFoundException e = thrown()

        and:
        e.message == /Driver url not found for name: "$name", version regexp: "${Pattern.quote(version)}", platform: "${baseDriverProperties.platform}", bit: "${DriverUrlsConfiguration.BITS[arch]}"/

        where:
        arch = X86_64
        name = 'chromedriver'
        version = '2.42.0'
        os = Linux.INSTANCE
        baseDriverProperties = [
            name: name,
            version: version,
            platform: DriverUrlsConfiguration.PLATFORMS[os]
        ]
    }

    DriverUrlsConfiguration parseConfiguration() {
        new DriverUrlsConfiguration(configurationFile)
    }

    private void configuration(String configuration) {
        configurationFile.text = configuration
    }

    private void configuration(Object configuration) {
        configurationFile.text = toJson(configuration)
    }

    private void emptyConfiguration() {
        configuration(drivers: [])
    }
}

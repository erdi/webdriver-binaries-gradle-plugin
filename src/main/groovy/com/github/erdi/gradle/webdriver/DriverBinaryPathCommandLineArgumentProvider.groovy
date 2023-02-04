/*
 * Copyright 2021 the original author or authors.
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

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Optional
import org.gradle.process.CommandLineArgumentProvider

class DriverBinaryPathCommandLineArgumentProvider implements CommandLineArgumentProvider {

    @Classpath
    @Optional
    final Provider<Directory> driverBinaryPropertiesDirectory

    DriverBinaryPathCommandLineArgumentProvider(Provider<Directory> driverBinaryPropertiesDirectory) {
        this.driverBinaryPropertiesDirectory = driverBinaryPropertiesDirectory
    }

    @Override
    @SuppressWarnings(['SpaceAfterClosingBrace', 'SpaceBeforeClosingBrace'])
    Iterable<String> asArguments() {
        if (!driverBinaryPropertiesDirectory.present) {
            return []
        }

        def properties = loadProperties()

        ["-D${properties['systemPropertyName']}=${properties['path']}"]
    }

    private Properties loadProperties() {
        driverBinaryPropertiesDirectory.map { propertiesDirectory ->
            propertiesDirectory.asFileTree.singleFile.withInputStream { inputStream ->
                new Properties().tap { properties ->
                    properties.load(inputStream)
                }
            } as Properties
        }.get()
    }

}

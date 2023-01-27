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

import groovy.transform.builder.Builder

import java.util.regex.Pattern

class DriverUrlNotFoundException extends Exception {

    @SuppressWarnings(['SpaceAfterClosingBrace', 'SpaceBeforeClosingBrace'])
    @Builder
    private DriverUrlNotFoundException(String name, Pattern version, String platform, Collection<String> bits, Collection<String> archs) {
        super(/Driver url not found for name: "$name", version regexp: "$version", platform: "$platform", bit: "${join(bits)}", arch: "${join(archs)}"/)
    }

    protected static String join(Collection<String> archsOrBits) {
        archsOrBits.join(' or ')
    }

}

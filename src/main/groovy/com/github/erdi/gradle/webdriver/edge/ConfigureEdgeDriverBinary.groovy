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
package com.github.erdi.gradle.webdriver.edge

import com.github.erdi.gradle.webdriver.task.ConfigureBinary
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

import static com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata.EDGEDRIVER

@SuppressWarnings(['AbstractClassWithPublicConstructor', 'AbstractClassWithoutAbstractMethod'])
abstract class ConfigureEdgeDriverBinary extends ConfigureBinary {

    @Inject
    ConfigureEdgeDriverBinary(ObjectFactory objectFactory) {
        super(objectFactory, EDGEDRIVER, 'edgedriver')
    }

}

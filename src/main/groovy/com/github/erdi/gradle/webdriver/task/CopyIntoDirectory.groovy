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
package com.github.erdi.gradle.webdriver.task

import com.github.erdi.gradle.webdriver.WebDriverBinariesPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

import javax.inject.Inject

@SuppressWarnings(['AbstractClassWithPublicConstructor'])
@DisableCachingByDefault(because = 'This is an I/O heavy task which would not benefit from caching')
abstract class CopyIntoDirectory extends DefaultTask {

    private final FileSystemOperations fileSystemOperations

    @Inject
    CopyIntoDirectory(FileSystemOperations fileSystemOperations) {
        this.fileSystemOperations = fileSystemOperations
        this.group = WebDriverBinariesPlugin.TASK_GROUP
    }

    @InputFiles
    abstract ConfigurableFileCollection getFiles()

    @OutputDirectory
    abstract DirectoryProperty getOutputDirectory()

    @TaskAction
    void copy() {
        fileSystemOperations.copy {
            from(files)
            into(outputDirectory)
        }
    }

}

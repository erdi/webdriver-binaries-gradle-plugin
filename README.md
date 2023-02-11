[![License](https://img.shields.io/badge/license-ASL2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Linux Build status](https://circleci.com/gh/erdi/webdriver-binaries-gradle-plugin.svg?style=shield&circle-token=a992594ce0896410bbf5533eff72746f983f0ae2)](https://circleci.com/gh/erdi/webdriver-binaries-gradle-plugin)
[![Windows Build status](https://ci.appveyor.com/api/projects/status/hmxq7cwxn56uavy9?svg=true)](https://ci.appveyor.com/project/erdi/webdriver-binaries-gradle-plugin-e2l29)

# WebDriver binaries Gradle plugin

This project contains a Gradle plugin that downloads WebDriver binaries specific to the operating system the build runs on.
The plugin also allows to configure tasks implementing `JavaForkOptions`, including `Test` tasks to use the downloaded binaries.

## Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.github.erdi.webdriver-binaries).

## Usage

### Extension properties

This plugin exposes the following optional properties through the extension named `webdriverBinaries`:

| Name                      | Type                                    | Description                                                                                                                                                                                                                                                                                                                                                                                                                             |
|---------------------------|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `chromedriver`            | `String` or `Pattern`                   | The exact version when `String` is used or the highest version matching the `Pattern` of ChromeDriver binary to be used by the project. No ChromeDriver binary will be downloaded if this property is not specified.                                                                                                                                                                                                                    |
| `geckodriver`             | `String` or `Pattern`                   | The exact version when `String` is used or the highest version matching the `Pattern` of GeckoDriver binary to be used by the project. No GeckoDriver binary will be downloaded if this property is not specified.                                                                                                                                                                                                                      |
| `edgedriver`              | `String` or `Pattern`                   | The exact version when `String` is used or the highest version matching the `Pattern` of EdgeDriver binary to be used by the project. No EdgeDriver binary will be downloaded if this property is not specified.                                                                                                                                                                                                                        |
| `downloadRoot`            | `File`                                  | The location at which the binaries should be cached after downloading. If not specified the binaries are cached in the Gradle user home directory. Should not be specified under normal circumstances to benefit from caching of the binaries between multiple project builds.                                                                                                                                                          |
| `driverUrlsConfiguration` | `org.gradle.api.resources.TextResource` | The text resource which contains mapping from a binary version to a URL. If not specified then the default is to use [WebDriver Extensions Maven Plugin's `package.json` file](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository/blob/master/repository-3.0.json) from `https://raw.githubusercontent.com/webdriverextensions/webdriverextensions-maven-plugin-repository/master/repository-3.0.json`. |
| `fallbackTo32Bit`         | `boolean`                               | Whether or not to fallback to a 32bit version of drivers if a 64bit version is not found. Defaults to `false`.                                                                                                                                                                                                                                                                                                                          |

Example usage:

    webdriverBinaries {
        chromedriver = '2.32'
        geckodriver = '0.19.0'
        edgedriver = '86.0.601.0'
    }

### Extension methods

#### Detailed binaries configuration methods

Apart properties which can be used for specifying driver binaries versions, the plugin exposes `chromedriver()`, `geckodriver()`, and `edgedriver()` configuration methods through the extension
named `webdriverBinaries`.
Each method takes an action which operates on an object with the following properties:

| Name                      | Type                                    | Description                                                                                                                                                                                                                                                                                                                                                                                                                             | 
|---------------------------|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `version`                 | `String` or `Pattern`                   | The exact version when `String` is used or the highest version matching the `Pattern` of the binary to be used by the project. No binary will be downloaded unless this property is specified.                                                                                                                                                                                                                                          |
| `architecture`            | String                                  | The architecture of the binary to be used. The allowed values are `X86`, `X86_64` and `ARM64`. Defaults to the architecture of the OS running the build.                                                                                                                                                                                                                                                                                |
| `downloadRoot`            | `File`                                  | The location at which the binaries should be cached after downloading. If not specified the binaries are cached in the Gradle user home directory. Should not be specified under normal circumstances to benefit from caching of the binaries between multiple project builds.                                                                                                                                                          |
| `driverUrlsConfiguration` | `org.gradle.api.resources.TextResource` | The text resource which contains mapping from a binary version to a URL. If not specified then the default is to use [WebDriver Extensions Maven Plugin's `package.json` file](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository/blob/master/repository-3.0.json) from `https://raw.githubusercontent.com/webdriverextensions/webdriverextensions-maven-plugin-repository/master/repository-3.0.json`. |
| `fallbackTo32Bit`         | `boolean`                               | Whether or not to fallback to a 32bit version of the driver if a 64bit version is not found. Defaults to `false`.                                                                                                                                                                                                                                                                                                                       |

Example usage:

    webdriverBinaries {
        chromedriver {
            version = '2.32'
            architecture = 'X86'
        }
        geckodriver {
            version = '0.19.0'
            architecture = 'X86'
        }
        edgedriver {
            version = '86.0.601.0'
            architecture = 'X86'
            fallbackTo32Bit = true
        }
    }

Example usage which shows how to configure the plugin to use the latest version of chromedriver:

    webdriverBinaries {
        chromedriver {
            version = ~'.*'
        }
    }

#### Configuring tasks with downloaded driver paths

It's possible to configure any task implementing `org.gradle.process.JavaForkOptions`, which includes `org.gradle.api.tasks.testing.Test` tasks, with locations of the downloaded binaries via system
properties.
Doing so means that the location of the WebDriver binary is picked up when the driver is being initialized inside the JVM forked by these tasks.
That's because a system property with a name specific to the given driver and a value being the path to the downloaded binary is added for the forked JVM.

To configure the `test` task with the location of the downloaded binaries the following can be used:

    webdriverBinaries {
        configureTask(tasks.named("test"))
    }

To configure all `Test` tasks in a given project:

    webdriverBinaries {
        configureTasks(tasks.withType(Test))
    }

To configure an arbitrary `JavaExec` task:

    def execTask = tasks.register("exec", JavaExec) {
        ...
    }
    
    webdriverBinaries {
        configureTask(execTask)
    }

Note that by default only the configured version of each binary is part of the inputs for the tasks configured as per the above examples. If you wish even for the version of the binary to not be part
of the inputs of the configured tasks then this can be achieved by adding the following snippet to your build script:

    import com.github.erdi.gradle.webdriver.task.ResolveDriverBinary

    normalization {
        runtimeClasspath {
            properties("${ResolveDriverBinary.PROPERTY_FILE_NAME_PREFIX}.*.properties") {
                ignoreProperty('version')
            }
        }
    }

### Tasks

This plugin adds the following tasks to the project:

* `resolveChromedriverBinary` - resolves a ChromeDriver binary from the configured cache location after downloading it if necessary; writes out a properties file with the system property name, path
  and version of the binary
* `resolveGeckodriverBinary` - resolves a GeckoDriver binary from the configured cache location after downloading it if necessary; writes out a properties file with the system property name, path and
  version of the binary
* `resolveEdgedriverBinary` - resolves a EdgeDriver binary from the configured cache location after downloading it if necessary; writes out a properties file with the system property name, path and
  version of the binary

There is usually no need to call the above tasks directly
if [the plugin was configured to add the necessary system properties to the required tasks implementing `JavaForkOptions`](#configuring-tasks-with-downloaded-driver-paths).

Note that a dependency on a resolve task for a given driver binary is not added unless a version of the binary for that particular driver is specified using one of the properties of `webdriverBinaries` extension.

### Configuring download URLs

By default, the plugin uses information
from [WebDriver Extensions Maven Plugin's `package.json` file](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository/blob/master/repository-3.0.json) to determine what URL
should a given binary be downloaded from.

If a version of a binary you would like to download using the plugin is not listed in the aforementioned file you can do one of the following:

* provide a pull request to [WebDriver Extensions Maven Plugin Repository 3.0](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository) which adds the version in question to
  the file - the URL you add will be visible to the plugin as soon as the PR gets merged
* author an own version of the `package.json` file and configure the plugin to use it

If you'd like to use the latter then after authoring your own version of `package.json` and dropping it, for example, in the root directory of your build you need to configure the plugin to use it:

    webdriverBinaries {
        driverUrlsConfiguration = resources.text.fromFile('package.json')
    }

Note that the `driverUrlsConfiguration` property is a `org.gradle.api.resources.TextResource` and can be configured with a text resource from various sources -
see [javadoc for `org.gradle.api.resources.TextResourceFactory`](https://docs.gradle.org/current/javadoc/org/gradle/api/resources/TextResourceFactory.html) for more examples.

## Building

### Importing into IDE

The project is set up to work with IntelliJ IDEA, simply import it using the native IntelliJ IDEA import for Gradle projects.

### Tests

If you import the project into IntelliJ as described above then you can run integration tests even after changing the code without having to perform any manual steps.
They are configured to run in an environment matching the one used when running them using Gradle on the command line.

### Checking the build

The project contains some code verification tasks aside from tests so if you wish to run a build matching the one on CI then execute `./gradlew check`.

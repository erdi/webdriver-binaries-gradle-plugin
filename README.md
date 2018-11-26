[![License](https://img.shields.io/badge/license-ASL2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Linux Build status](https://circleci.com/gh/erdi/webdriver-binaries-gradle-plugin.svg?style=shield&circle-token=a992594ce0896410bbf5533eff72746f983f0ae2)](https://circleci.com/gh/erdi/webdriver-binaries-gradle-plugin)
[![Windows Build status](https://ci.appveyor.com/api/projects/status/hmxq7cwxn56uavy9?svg=true)](https://ci.appveyor.com/project/erdi/webdriver-binaries-gradle-plugin-e2l29)
# WebDriver binaries Gradle plugin

This project contains a Gradle plugin that downloads WebDriver binaries specific to the operating system the build runs on.
The plugin also as configures various parts of the build to use the downloaded binaries.

## Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.energizedwork.webdriver-binaries).

## Usage

### Extension properties

This plugin exposes the following optional properties through the extension named `webdriverBinaries`:

| Name | Type | Description |
| --- | --- | --- |
| `chromedriver` | `String` | The version of ChromeDriver binary to be used by the project. No ChromeDriver binary will be downloaded if this property is not specified. |
| `geckodriver` | `String` | The version of GeckoDriver binary to be used by the project. No GeckoDriver binary will be downloaded if this property is not specified. |
| `iedriverserver` | `String` | The version of IEDriverServer binary to be used by the project. No IEDriverServer binary will be downloaded if this property is not specified. |
| `downloadRoot` |`File`| The location into which the binaries should be downloaded. If not specified the binaries are downloaded into the Gradle user home directory. Should not be specified under normal circumstances to benefit from caching of the binaries between multiple project builds. |
| `driverUrlsConfiguration` |`org.gradle.api.resources.TextResource`| The text resource which contains mapping from a binary version to a URL. If not specified then the default is to use [WebDriver Extensions Maven Plugin's `package.json` file](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository/blob/master/repository-3.0.json) from `https://raw.githubusercontent.com/webdriverextensions/webdriverextensions-maven-plugin-repository/master/repository-3.0.json`. |

Example usage:

    webdriverBinaries {
        chromedriver '2.32'
        geckodriver '0.19.0'
        iedriverserver '3.8.0'
    }

### Extension methods

#### Detailed binaries configuration methods

Additionally to properties which can be used for specifying driver binaries versions, the plugin exposes `chromedriver()`, `geckodriver()` and `iedriverserver()` configuration methods through the the extension named `webdriverBinaries`.
Each method takes a closure which delegates to an object with the following properties: 

| Name | Type | Description | 
| --- | --- | --- |
| `version` | String | The version of binary to be used by the project. No binary will be downloaded if this property is not specified. |
| `architecture` | String | The architecture of the binary to be used. The allowed values are `X86` and `X86_64`. Defaults to the architecture of the OS running the build. |

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
        iedriverserver {
            version = '3.8.0'
            architecture = 'X86'
        }
    }

#### Configuring additional tasks with downloaded driver paths

By default the plugin configures all `org.gradle.api.tasks.testing.Test` tasks with locations of the downloaded binaries via system properties but additional tasks can also be configured as along as they implement `org.gradle.process.JavaForkOptions` - `org.gradle.api.tasks.JavaExec` is an example of such task.

Example usage:

    task exec(type: JavaExec) {
        ...
    }
    
    webdriverBinaries {
        configureTask(exec)
    }

### Tasks

This plugin adds the following tasks to the project:
 * `configureChromeDriverBinary` - downloads, caches and configures the build to use a ChromeDriver binary
 * `configureGeckoDriverBinary` - downloads, caches and configures the build to use a GeckoDriver binary
 * `configureIeDriverServerBinary` - downloads, caches and configures the build to use a IEDriverServer binary

There is no need to call the above tasks directly because the plugin interweaves them into the build lifecycle by configuring all `org.gradle.api.tasks.testing.Test` tasks to depend on them.

Note that a configure task for a given driver binary is skipped unless a version of the binary for that particular driver is specified using one of the properties of `webdriverBinaries` extension.

When a configuration task is executed it modifies configuration of system properties for all `org.gradle.api.tasks.testing.Test` tasks in the project so that the location of the WebDriver binary location is picked up when the driver is being initialized.
That is, it adds a system property with a name specific to the given driver and a value being the path to the downloaded binary. 

### Configuring download URLs

By default, the plugin uses information from [WebDriver Extensions Maven Plugin's `package.json` file](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository/blob/master/repository-3.0.json) to determine what URL should a given binary be downloaded from.

If a version of a binary you would like to download using the plugin is not listed in the aforementioned file you can do one of the following:
* provide a pull request to [WebDriver Extensions Maven Plugin Repository 3.0](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository) which adds the version in question to the file - the URL you add will be visible to the plugin as soon as the PR gets merged
* author an own version of the `package.json` file and configure the plugin to use it

If you'd like to use the latter then after authoring your own version of `package.json` and dropping it, for example, in the root directory of your build you need to configure the plugin to use it:

    webdriverBinaries {
        driverUrlsConfiguration = resources.text.fromFile('package.json')
    }
    
Note that the `driverUrlsConfiguration` property is a `org.gradle.api.resources.TextResource` and can be configured with a text resource from various sources - see [javadoc for `org.gradle.api.resources.TextResourceFactory`](https://docs.gradle.org/current/javadoc/org/gradle/api/resources/TextResourceFactory.html) for more examples.  

### Integration with Idea configuration extensions plugin (com.github.erdi.extended-idea)

If [Idea configuration extensions plugin](https://github.com/erdi/idea-gradle-plugins#idea-configuration-extensions-plugin) is applied to the project together with this plugin it will do the following:
* configure the `ideaWorkspace` task added to the build by [Gradle's built-in IDEA plugin](https://docs.gradle.org/current/userguide/idea_plugin.html) to depend on `configureChromeDriverBinary`, `configureGeckoDriverBinary` and `configureIeDriverServerBinary` tasks
* add system properties specific for the drivers, setting the path to the downloaded binaries as their values, to default default JUnit run configuration in IntelliJ when the configuration tasks are executed

The above will ensure that locations of driver binaries are picked up when running tests from IntelliJ.   

## Building

### Importing into IDE

The project is setup to generate IntelliJ configuration files.
Simply run `./gradlew idea` and open the generated `*.ipr` file in IntelliJ.

### Tests

If you import the project into IntelliJ as described above then you can run integration tests even after changing the code without having to perform any manual steps.
They are configured to run in an environment matching the one used when running them using Gradle on the command line.

### Checking the build

The project contains some code verification tasks aside from tests so if you wish to run a build matching the one on CI then execute `./gradlew check`.

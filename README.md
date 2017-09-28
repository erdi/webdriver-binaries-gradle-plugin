[![License](https://img.shields.io/badge/license-ASL2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Linux Build status](https://circleci.com/gh/energizedwork/webdriver-binaries-gradle-plugin.svg?style=shield&circle-token=a992594ce0896410bbf5533eff72746f983f0ae2)](https://circleci.com/gh/energizedwork/webdriver-binaries-gradle-plugin)
[![Windows Build status](https://ci.appveyor.com/api/projects/status/xg10l6d6x0fant1c?svg=true)](https://ci.appveyor.com/project/erdi/webdriver-binaries-gradle-plugin)
# WebDriver binaries Gradle plugin

This project contains a Gradle plugin that downloads and caches WebDriver binaries specific to the OS the build runs on.
The plugin also as configures various aspects of the build to use the downloaded binaries.

## Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.energizedwork.webdriver-binaries).

## Usage

### Extension properties

This plugin exposes the following optional properties through the extension named `webdriverBinaries`:

| Name | Type | Description |
| --- | --- | --- |
| `chromedriver` | `String` | The version of ChromeDriver binary to be used by the project. No ChromeDriver binary will be downloaded if this property is not specified. |
| `geckodriver` | `String` | The version of GeckoDriver binary to be used by the project. No GeckoDriver binary will be downloaded if this property is not specified. |
| `downloadRoot` |`File`| The location into which the binaries should be downloaded. If not specified the binaries are downloaded into the Gradle user home directory. Should not be specified under normal circumstances to benefit from caching of the binaries between multiple builds. |

Example usage:

    webdriverBinaries {
        chromedriver '2.32'
        geckodriver '0.19.0'
    }

### Tasks

This plugin adds the following tasks to the project:
 * `configureChromeDriverBinary` - downloads, caches and configures the build to use a ChromeDriver binary
 * `configureGeckoDriverBinary` - downloads, caches and configures the build to use a GeckoDriver binary

There is no need to call the above tasks directly because the plugin interweaves them into the build lifecycle by configuring all `org.gradle.api.tasks.testing.Test` tasks to depend on them.

Note that a configure task for a given driver binary is skipped unless a version of the binary for that particular driver is specified using a `webdriverBinaries` extension property.

When a configuration task it is executed it adds a system property specific for the given driver setting the path to the downloaded binary as its value to all `org.gradle.api.tasks.testing.Test` tasks in the project so that the binary location is picked up when running the tests. 

### Integration with Idea JUnit plugin (com.energizedwork.idea-junit)

If [Idea JUnit plugin](https://github.com/energizedwork/idea-gradle-plugins#idea-junit-plugin) is applied to the project together with this plugin it will do the following:
* configure the `ideaWorkspace` task added to the build by [ Gradle's built-in IDEA plugin](https://docs.gradle.org/current/userguide/idea_plugin.html) to depend on `configureChromeDriverBinary` and `configureGeckoDriverBinary` tasks
* add system properties specific for the drivers setting the path to the downloaded binaries as their values to default default JUnit run configuration in IntelliJ when the configuration tasks are executed

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

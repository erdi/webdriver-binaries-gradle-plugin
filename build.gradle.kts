import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    idea
    `java-gradle-plugin`
    groovy
    codenarc
    id("com.gradle.plugin-publish") version "0.12.0"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    codenarc("org.codenarc:CodeNarc:2.1.0") {
        exclude(module = "GMetrics")
        exclude(group = "org.codehaus.groovy")
    }
    codenarc("org.codehaus.groovy:groovy-all:3.0.7")

    implementation("org.ysb33r.gradle:grolifant50:1.3.2")
    implementation("org.apache.maven:maven-artifact:3.6.3")

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0") {
        exclude(module = "groovy")
    }
}

version = "2.7"

idea {
    project {
        jdkName ="1.8"
    }
}

codenarc.configFile = file("gradle/codenarc/rulesets.groovy")

tasks.register<Test>("endToEndTest") {
    classpath = files(sourceSets.test.map { it.runtimeClasspath }, "src/e2e/resources")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxHeapSize = "1g"
    jvmArgs("-XX:MaxMetaspaceSize=128m")
    testLogging {
        exceptionFormat = FULL
    }
}

tasks.withType<GroovyCompile>().configureEach {
    groovyOptions.forkOptions.memoryMaximumSize = "256m"
}

gradlePlugin {
    plugins {
        create("webDriverBinariesPlugin") {
            id = "com.github.erdi.webdriver-binaries"
            implementationClass = "com.github.erdi.gradle.webdriver.WebDriverBinariesPlugin"
            displayName = "WebDriver Binaries Plugin"
            description = "A plugin that downloads and caches WebDriver binaries specific to the OS the build runs on."
        }
    }
}

pluginBundle {
    vcsUrl = "https://github.com/erdi/webdriver-binaries-gradle-plugin"
    website = "https://github.com/erdi/webdriver-binaries-gradle-plugin/blob/master/README.md"
    tags = listOf("WebDriver", "selenium", "test", "chromedriver", "geckodriver")
}
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    idea
    `java-gradle-plugin`
    groovy
    codenarc
    id("com.gradle.plugin-publish") version "1.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    codenarc("org.codenarc:CodeNarc:3.2.0") {
        exclude(module = "GMetrics")
    }
    codenarc("org.codehaus.groovy:groovy-all:3.0.13")

    implementation("org.ysb33r.gradle:grolifant-herd:4.0.0")
    implementation("org.apache.maven:maven-artifact:3.8.7")

    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0") {
        exclude(module = "groovy")
    }
}

group = "com.github.erdi"
version = "3.2"

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
    vcsUrl.set("https://github.com/erdi/webdriver-binaries-gradle-plugin")
    website.set("https://github.com/erdi/webdriver-binaries-gradle-plugin/blob/master/README.md")
    plugins {
        create("webDriverBinariesPlugin") {
            id = "com.github.erdi.webdriver-binaries"
            implementationClass = "com.github.erdi.gradle.webdriver.WebDriverBinariesPlugin"
            displayName = "WebDriver Binaries Plugin"
            description = "A plugin that downloads and caches WebDriver binaries specific to the OS the build runs on."
            tags.set(setOf("WebDriver", "selenium", "test", "chromedriver", "geckodriver"))
        }
    }
}
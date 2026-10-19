import org.gradle.api.artifacts.dsl.LockMode
import org.gradle.api.tasks.wrapper.Wrapper

plugins {
    `java-library`
}

group = "io.github.opendop"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.protobuf:protobuf-java:4.35.1")
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.STRICT)
}

val contractsDirectory = layout.projectDirectory.dir("..")
val generatedJavaDirectory = contractsDirectory.dir("build/generated/java")

val generateContracts = tasks.register<Exec>("generateContracts") {
    description = "Generates the Java and Python contracts with the pinned Buf plugins."
    workingDir(contractsDirectory)
    commandLine("buf", "generate")
    inputs.dir(contractsDirectory.dir("proto"))
    inputs.files(contractsDirectory.file("buf.yaml"), contractsDirectory.file("buf.gen.yaml"))
    outputs.dir(generatedJavaDirectory)
    outputs.dir(contractsDirectory.dir("build/generated/python"))
}

sourceSets.main {
    java.srcDir(generatedJavaDirectory)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.compileJava {
    dependsOn(generateContracts)
}

val consumptionTest = tasks.register<JavaExec>("consumptionTest") {
    group = "verification"
    description = "Verifies generated EventMetadata construction and binary round-trip."
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.github.opendop.contracts.EventMetadataConsumption")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
}

tasks.test {
    // The standalone verifier replaces the framework-based Test runner.
    enabled = false
    dependsOn(consumptionTest)
}

tasks.register<JavaExec>("writeJavaInteropFixture") {
    group = "verification"
    description = "Writes a fresh Java EventMetadata for the Python reader."
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.github.opendop.contracts.EventMetadataInterop")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    args("write", contractsDirectory.asFile.absolutePath)
}

tasks.register<JavaExec>("verifyPythonInterop") {
    group = "verification"
    description = "Checks fresh and golden Python messages with the Java reader."
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.github.opendop.contracts.EventMetadataInterop")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    args("verify", contractsDirectory.asFile.absolutePath)
}

tasks.wrapper {
    gradleVersion = "9.8.0"
    distributionType = Wrapper.DistributionType.BIN
    distributionSha256Sum = "bafd5ce9cfaea0fbccfdc8439a1ac42fbd4cd9c89dc9a988228d8a2639a58e6c"
}

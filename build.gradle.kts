plugins {
    kotlin("jvm") version "1.8.0"
    application
    id("org.jetbrains.dokka") version "1.8.10"
}

group = "com.logoped"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:1.6.8")
    implementation("io.ktor:ktor-server-netty:1.6.8")
    implementation("io.ktor:ktor-html-builder:1.6.8")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    testImplementation("io.ktor:ktor-server-test-host:1.6.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

kotlin {
    jvmToolchain(8)
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
    moduleName.set("Логопедический помощник")

    dokkaSourceSets {
        named("main") {
            includes.from("README.md")
            displayName.set("Логопедический помощник")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
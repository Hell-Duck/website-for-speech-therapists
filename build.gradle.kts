plugins {
    kotlin("jvm") version "1.9.22"
    application
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "com.logoped"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.3.12"
    val exposedVersion = "0.44.1"

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
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

application {
    mainClass.set("MainKt")
    applicationDefaultJvmArgs = listOf(
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED"
    )
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("jvm") version "1.5.0"
}

application.mainClass.set("me.melijn.monitorflux.MonitorKt")
group = "me.melijn.monitorflux"
version = "1.0.0"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}

val ktor = "1.5.4"
val kotlin = "1.5.0"
val kotlinX = "1.5.0"

dependencies {
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinX")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-jdk8
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinX")

    // https://mvnrepository.com/artifact/org.influxdb/influxdb-java
    implementation("org.influxdb:influxdb-java:2.21")

    // https://github.com/cdimascio/dotenv-kotlin
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // https://mvnrepository.com/artifact/io.ktor/ktor-client-okhttp
    implementation("io.ktor:ktor:$ktor")
    implementation("io.ktor:ktor-client-okhttp:$ktor")
    implementation("io.ktor:ktor-client-jackson:$ktor")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib-jdk8
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.3")

    // https://github.com/FasterXML/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")

}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "15"
        }
    }

    shadowJar {
        archiveFileName.set("stats.jar")
    }
}

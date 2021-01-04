import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jetbrains.kotlin.jvm") version "1.4.21-2"
}

application {
    mainClassName = "me.melijn.monitorflux.MonitorKt"
}
group = "me.melijn.monitorflux"
version = "1.0.0"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

repositories {
    jcenter()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-jdk8
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.2")

    // https://mvnrepository.com/artifact/org.influxdb/influxdb-java
    implementation("org.influxdb:influxdb-java:2.21")

    // https://github.com/cdimascio/dotenv-kotlin
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // https://mvnrepository.com/artifact/io.ktor/ktor-client-okhttp
    implementation("io.ktor:ktor:1.4.3")
    implementation("io.ktor:ktor-client-okhttp:1.5.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib-jdk8
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.21")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")

    // https://github.com/FasterXML/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")

}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "14"
        }
    }

    shadowJar {
        archiveFileName.set("stats.jar")
    }
}

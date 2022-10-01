@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.7.20"
    id("io.ktor.plugin") version "2.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
}

group = "com.studiia"
version = "0.0.1"
application {
    mainClass.set("com.studiia.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral {
        metadataSources {
            mavenPom()
            artifact()
        }
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.1.2")
    implementation("io.ktor:ktor-server-auth-jvm:2.1.2")
    implementation("io.ktor:ktor-server-sessions-jvm:2.1.2")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:2.1.2")
    implementation("io.ktor:ktor-server-double-receive-jvm:2.1.2")
    implementation("io.ktor:ktor-server-locations-jvm:2.1.2")
    implementation("io.ktor:ktor-server-host-common-jvm:2.1.2")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.1.2")
    implementation("io.ktor:ktor-server-compression-jvm:2.1.2")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.1.2")
    implementation("io.ktor:ktor-server-cors-jvm:2.1.2")
    implementation("io.ktor:ktor-server-hsts-jvm:2.1.2")
    implementation("io.ktor:ktor-server-http-redirect-jvm:2.1.2")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.1.2")
    implementation("io.ktor:ktor-server-call-id-jvm:2.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.1.2")
    implementation("io.ktor:ktor-server-netty-jvm:2.1.2")
    implementation("ch.qos.logback:logback-classic:1.4.1")
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("io.github.microutils:kotlin-logging:3.0.0")
    implementation("net.axay:simplekotlinmail-core:1.4.0")
    implementation("net.axay:simplekotlinmail-client:1.4.0")
    implementation("org.kodein.di:kodein-di:7.14.0")
    implementation("io.kotest:kotest-runner-junit5:5.4.2")
    implementation("io.kotest:kotest-assertions-core:5.4.2")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.1.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.20")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
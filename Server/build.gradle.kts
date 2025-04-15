group = "rhx.dol"
version = "1.0.3"

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.toml)
    testImplementation(libs.bundles.test)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

ktor {
    docker {
        localImageName = "degrees-of-management"
        jreVersion = JavaVersion.VERSION_21
    }
}

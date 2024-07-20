plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(group = "javax.inject", name = "javax.inject", version = "1")
    implementation(libs.symbol.processing.api)
}

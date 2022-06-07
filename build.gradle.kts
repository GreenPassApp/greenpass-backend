import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.owasp.dependencycheck") version "7.1.0.1"
    //id("org.jetbrains.dokka") version "1.6.21" //disabled because of https://github.com/Kotlin/dokka/issues/2488
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
}

group = "eu.greenpassapp"
version = "1.1.3"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("se.digg.dgc:dgc-create-validate:1.0.2")
    implementation("se.digg.dgc:dgc-schema:1.0.2")
    implementation("com.ryantenney.passkit4j:passkit4j:2.0.1")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.bouncycastle:bcmail-jdk15on:1.70")
    implementation("com.beust:klaxon:5.6")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.17.2")
    //dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.20") //disabled because of https://github.com/Kotlin/dokka/issues/2488
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

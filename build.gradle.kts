plugins {
    kotlin("jvm") version "2.2.21"
    `java-library`
    `maven-publish`
}

group = "com.ontometrics"
version = "3.0.0"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    // AWS SDK for Java v2
    implementation("software.amazon.awssdk:dynamodb:2.30.31")

    // Local S2 Geometry Library
    implementation(files("lib/s2-geometry-library/s2-geometry-java.jar"))

    // Local Guava r09
    implementation(files("lib/guava-r09/guava-r09.jar"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "dynamodb-geo-aws-sdk-2"
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/eHawk-Inc/dynamodb-geo-aws-sdk-2")
            credentials {
                username = "eHawk-Inc"
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}


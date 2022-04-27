import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Deploying to OSSRH with Gradle
// https://central.sonatype.org/pages/gradle.html
// https://github.com/gradle-nexus/publish-plugin

// Did you update version number here AND in the README?
// This is different from other projects because it is TEST SCOPED.

// To find out if any dependencies need upgrades:
// gradle --refresh-dependencies dependencyUpdates

// To publish to maven local:
// gradle --warning-mode all clean assemble dokkaJar publish publishToMavenLocal

// To publish to Sonatype (do the maven local above first):
// gradle --warning-mode all clean test assemble dokkaJar publish publishToSonatype closeAndReleaseSonatypeStagingRepository

// If half-deployed, sign in here:
// https://oss.sonatype.org
// Click on "Staging Repositories"
// Open the "Content" for the latest one you uploaded.
// If it looks good, "Close" it and wait.
// When it's really "closed" with no errors, "Release" (and automatically drop) it.
//
// Alternatively, if you can see it here, then it's ready to be "Closed" and deployed manually:
// https://oss.sonatype.org/content/groups/staging/org/organicdesign/TestUtilsBasic/
// Here once released:
// https://repo1.maven.org/maven2/org/organicdesign/TestUtilsBasic/

// https://docs.gradle.org/current/userguide/build_environment.html
// You must have the following set in ~/.gradle/gradle.properties
// sonatypeUsername=
// sonatypePassword=
//
// At least while dokka crashes the gradle daemon you also want:
// org.gradle.daemon=false
// Or run with --no-daemon
plugins {
    `maven-publish`
    signing
    id("com.github.ben-manes.versions") version "0.42.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jetbrains.dokka") version "1.6.21"
    kotlin("jvm") version "1.6.21"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

group = "org.organicdesign"
// Remember to update the version number in both the Maven and Gradle imports in README.md
version = "0.0.2"
description = "Utilities for testing common Java contracts: equals(), hashCode(), compare(), compareTo(), and serialization"

java {
//    withJavadocJar()
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("dokkaJar") {
    archiveClassifier.set("javadoc")
    dependsOn("dokkaJavadoc")
    from("$buildDir/dokka/javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            afterEvaluate {
                artifactId = tasks.jar.get().archiveBaseName.get()
            }
            artifact(tasks["dokkaJar"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
//            artifact(tasks["dokkaJar"])
            pom {
                name.set(rootProject.name)
                packaging = "jar"
                description.set(project.description)
                url.set("https://github.com/GlenKPeterson/TestUtilsBasic")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://apache.org/licenses/LICENSE-2.0.txt")
                    }
                    license {
                        name.set("The Eclipse Public License v. 2.0")
                        url.set("https://eclipse.org/legal/epl-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("GlenKPeterson")
                        name.set("Glen K. Peterson")
                        email.set("glen@organicdesign.org")
                        organization.set("PlanBase Inc.")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/GlenKPeterson/TestUtilsBasic.git")
                    developerConnection.set("scm:git:https://github.com/GlenKPeterson/TestUtilsBasic.git")
                    url.set("https://github.com/GlenKPeterson/TestUtilsBasic.git")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

tasks.compileJava {
    options.encoding = "UTF-8"
}
repositories {
    mavenLocal()
    mavenCentral()
    maven(url="https://jitpack.io")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}
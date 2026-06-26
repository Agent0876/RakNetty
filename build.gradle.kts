plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    `maven-publish`
    signing
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    group   = "io.github.agent0876.raknetty"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }

    dependencies {
        "testImplementation"(kotlin("test"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    // Sources & Javadoc JARs (Maven Central 필수 요건)
    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>()["main"].allSource)
    }

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
        // Kotlin 프로젝트에서는 빈 javadoc jar 허용
        from(tasks.named("javadoc"))
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set(project.name)
                    description.set("Netty-based RakNet protocol implementation in Kotlin")
                    url.set("https://github.com/Agent0876/RakNetty")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("agent0876")
                            name.set("Agent0876")
                            email.set("shinseungmin070920@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/Agent0876/RakNetty.git")
                        developerConnection.set("scm:git:ssh://github.com/Agent0876/RakNetty.git")
                        url.set("https://github.com/Agent0876/RakNetty")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "CentralPortal"
                val releasesUrl = uri("https://central.sonatype.com/api/v1/publisher/upload")
                url = releasesUrl
                credentials {
                    username = providers.gradleProperty("sonatypeUsername")
                        .orElse(providers.environmentVariable("SONATYPE_USERNAME"))
                        .orNull
                    password = providers.gradleProperty("sonatypePassword")
                        .orElse(providers.environmentVariable("SONATYPE_PASSWORD"))
                        .orNull
                }
            }
        }
    }

    extensions.configure<SigningExtension> {
        val signingKey = providers.gradleProperty("signing.key")
            .orElse(providers.environmentVariable("SIGNING_KEY"))
            .orNull
        val signingPassword = providers.gradleProperty("signing.password")
            .orElse(providers.environmentVariable("SIGNING_PASSWORD"))
            .orNull
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
        sign(extensions.getByType<PublishingExtension>().publications["mavenJava"])
    }
}

import org.glavo.gradle.UncompressResources

plugins {
    id("java-library")
    id("jacoco")
    id("maven-publish")
}

group = "org.glavo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lz4)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.opennbt)
    testImplementation(libs.lz4)
    testImplementation(libs.xz)
    testImplementation(libs.commons.io)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.compileJava {
    options.release.set(17)
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).also {
        it.encoding("UTF-8")
        it.addStringOption("link", "https://docs.oracle.com/en/java/javase/17/docs/api/")
        it.addBooleanOption("html5", true)
        it.addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

val uncompressResources by tasks.registering(UncompressResources::class) {
    inputDir.set(project.layout.projectDirectory.dir("src/test/resources-compressed"))
    outputDir.set(project.layout.buildDirectory.dir("generated/test/resources-uncompressed"))
}

tasks.processTestResources {
    dependsOn(uncompressResources)
    from(uncompressResources.map { it.outputDir })
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

publishing.publications.create<MavenPublication>("maven") {
    groupId = project.group.toString()
    version = project.version.toString()
    artifactId = project.name

    from(components["java"])

    pom {
        name.set(project.name)
        description.set(project.description)
        url.set("https://github.com/Glavo/HelloNBT")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("Glavo")
                name.set("Glavo")
                email.set("zjx001202@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/Glavo/HelloNBT")
        }
    }
}

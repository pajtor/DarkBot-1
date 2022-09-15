plugins {
    java
    application
    `maven-publish`

    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.beryx.runtime") version "1.12.7"
}

buildscript {
    dependencies {
        classpath("com.guardsquare", "proguard-gradle", "7.2.2")
    }
}

group = "eu.darkbot"
version = "1.17.108"
description = "DarkBot"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

application {
    applicationName = "DarkBot"
    mainClass.set("com.github.manolo8.darkbot.Bot")
}

repositories {
    mavenLocal()
    mavenCentral()

    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://oss.jfrog.org/artifactory/oss-snapshot-local/com/formdev/") }
}

dependencies {
    val apiVersion = "0.5.0"

    // use this if you want to use local(mavenLocal) darkbot API
    //implementation("eu.darkbot", "darkbot-impl", apiVersion)
    implementation("eu.darkbot.DarkBotAPI", "darkbot-impl", apiVersion)
    implementation("com.google.code.gson", "gson", "2.8.9")
    implementation("com.formdev", "flatlaf", "0.36")
    implementation("com.miglayout", "miglayout", "3.7.4")
    implementation("org.jgrapht", "jgrapht-core", "1.3.0")
    implementation("org.mvel", "mvel2", "2.4.4.Final")

    compileOnly("org.jetbrains", "annotations", "23.0.0")
    testCompileOnly("org.jgrapht", "jgrapht-io", "1.3.0")
}

publishing {
    java.withSourcesJar()

    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() { options.encoding = "UTF-8" }

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    allowaccessmodification()
    dontoptimize()
    dontobfuscate()
    dontnote()
    dontwarn()

    keepattributes("Signature")
    keep("class com.github.manolo8.** { *; }")
    keep("class eu.darkbot.** { *; }")
    keep("class com.formdev.** { *; }")

    injars(tasks["shadowJar"].outputs.files.singleFile)
    outjars("build/DarkBot.jar")

    if (JavaVersion.current().isJava9Compatible) {
        libraryjars("${System.getProperty("java.home")}/jmods")
    } else {
        libraryjars("${System.getProperty("java.home")}/lib/rt.jar")
    }

    dependsOn(tasks.build)
}

//will execute proguard task after build
//tasks.build {
//    finalizedBy(":proguard")
//}

// need to download WiX tools!
runtime {
    //options.addAll("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    modules.addAll(
        "java.desktop",
        "java.scripting",
        "java.logging",
        "java.xml",
        "java.datatransfer",
        "jdk.crypto.cryptoki"
    )
    jpackage {
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerType = "msi"
            installerOptions.addAll(
                listOf(
                    "--win-per-user-install",
                    "--win-shortcut",
                    "--win-menu"
                )
            )
        } else installerOptions.addAll(listOf("--icon", "icon.ico")) //not possible with .msi type

        imageOptions.addAll(listOf("--icon", "icon.ico"))
    }
}
plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
    kotlin("jvm") version "2.0.21"
    `maven-publish`
}

val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val mod_version: String by project
val maven_group: String by project
val archives_base_name: String by project
val fabric_version: String by project
val fabric_kotlin_version: String by project
val cobblemon_version: String by project
// Curse Maven file ID for Cobblemon v1.7.3+1.21.1 (Fabric). If you're on a
// different Cobblemon version, find the matching file ID on CurseForge's
// file list (https://www.curseforge.com/minecraft/mc-mods/cobblemon/files)
// — open the version you want, and the file ID is in the URL/download link.
val cobblemon_curse_file_id: String by project

version = mod_version
group = maven_group
base.archivesName.set(archives_base_name)

repositories {
    mavenCentral()
    maven {
        name = "CurseMaven"
        url = uri("https://www.cursemaven.com")
        content { includeGroup("curse.maven") }
    }
}

dependencies {
    // Minecraft / Fabric toolchain
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    // Fabric Language Kotlin (required by both this mod and Cobblemon)
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")

    // Cobblemon, via Curse Maven. This pins an exact build of Cobblemon
    // 1.7.3+1.21.1 (Fabric), which is the most reliable way to get a
    // reproducible compile-time dependency on a mod that isn't consistently
    // published to a conventional Maven repository for every version.
    //
    // Alternative: if you'd rather depend on Cobblemon's own Maven Central
    // artifacts (when available for your target version), swap this for:
    //   modImplementation("com.cobblemon:fabric:$cobblemon_version")
    // and remove the CurseMaven repository block above.
    modImplementation("curse.maven:cobblemon-687131:$cobblemon_curse_file_id")
}

loom {
    accessWidenerPath.set(file("src/main/resources/cobblemonmaxivs.accesswidener"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
}

tasks.processResources {
    inputs.property("version", mod_version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to mod_version))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options

plugins {
    kotlin("jvm")
    id("forgified-fabric-loom")
    id("com.matthewprenger.cursegradle")
}

repositories {
    maven("https://maven.shedaniel.me/")
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
}

loom {
    silentMojangMappingsLicense()
    accessWidener = file("src/main/resources/ingameime.accessWidener")
}

//General
val minecraft_version: String by rootProject
//Mod Props
val archives_base_name: String by project
val mod_version: String by rootProject
val maven_group: String by rootProject
//Fabric
val fabric_api_version: String by project
val fabric_loader_version: String by project
val cloth_api_version: String by project
val satin_version: String by project
val roughlyenoughitems: String by project
val fabric_kotlin_version: String by project

version = mod_version
group = maven_group
base {
    archivesBaseName = "$archives_base_name-$name-$minecraft_version"
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings(minecraft.officialMojangMappings())
    
    //Fabric
    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_api_version}")
    //REI
    modImplementation("me.shedaniel:RoughlyEnoughItems:${roughlyenoughitems}")
    //Cloth Api
    modImplementation("me.shedaniel.cloth:cloth-events:${cloth_api_version}")
    //Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:${fabric_kotlin_version}")
}

sourceSets {
    main {
        java {
            srcDirs(
                "src/main/java",
                "src/main/kotlin",
                "../common/src/main/kotlin"
            )
        }
        resources {
            srcDirs(
                "src/main/resources",
                "../common/src/main/resources"
            )
        }
    }
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        inputs.property("version", version)
    }
}

val changeLog: String by rootProject

curseforge {
    apiKey = rootProject.ext["apiKey"]
    project(closureOf<CurseProject> {
        id = "440032"
        releaseType = "release"
        changelog = changeLog
        mainArtifact(tasks["remapJar"])
        addArtifact(tasks["jar"])
        addGameVersion("Fabric")
        addGameVersion("Java 8")
        addGameVersion("1.15")
        addGameVersion("1.15.1")
        addGameVersion("1.15.2")
        relations(closureOf<CurseRelation> {
            requiredDependency("fabric-language-kotlin")
            requiredDependency("fabric-api")
            requiredDependency("cloth-api")
        })
    })
    
    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
}
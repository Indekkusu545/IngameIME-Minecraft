architectury { common(); injectInjectables = false }

repositories {
    maven("https://maven.fabricmc.net")
}

loom {
    accessWidener = file("../fabric/src/main/resources/ingameime.accessWidener")
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:0.11.3")
    //Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:1.5.+")
    //Cloth Config
    modImplementation("me.shedaniel.cloth:config-2:2.14.2") {
        exclude("net.fabricmc.fabric-api")
    }
}
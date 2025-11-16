import me.modmuss50.mpp.ReleaseType
import org.gradle.kotlin.dsl.assign
import java.util.*

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("me.modmuss50.mod-publish-plugin")
    id("com.github.johnrengelman.shadow")
}

val minecraft = stonecutter.current.version
val loader = loom.platform.get().name.lowercase()

version = "${mod.version}+$minecraft"
group = mod.group
base {
    archivesName.set("${mod.id}-$loader")
}

architectury.common(stonecutter.tree.branches.mapNotNull {
    if (stonecutter.current.project !in it) null
    else it.prop("loom.platform")
})
repositories{
    maven("https://repo1.maven.org/maven2/");
    maven("https://maven.neoforged.net/releases/")
    //modmenu
    maven("https://maven.terraformersmc.com/")
    //placeholder api (modmenu depencency)
    maven("https://maven.nucleoid.xyz/")
}
val accessWidenerFile = if (stonecutter.eval(minecraft, "<=1.21.5")) {
    "imaginebook.old.accesswidener"
}
else{
    "imaginebook.1.21.5.accesswidener"
}
dependencies {
    minecraft("com.mojang:minecraft:$minecraft")

    "org.sejda.imageio:webp-imageio:0.1.6".let{
        implementation(it)
        include(it)
    }

    if (loader == "fabric") {
        modImplementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
        mappings("net.fabricmc:yarn:$minecraft+build.${mod.dep("yarn_build")}:v2")
        modImplementation("com.terraformersmc:modmenu:${mod.dep("modmenu_version")}")

        //some features (like automatic resource loading from non vanilla namespaces) work only with fabric API installed
        //for example translations from assets/modid/lang/en_us.json won't be working, same stuff with textures
        //but we keep runtime only to not accidentally depend on fabric's api, because it doesn't exist in neo/forge
        modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_version")}")

        //if this is here if forgot to remove it
        modCompileOnly("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_version")}")

    }
    if (loader == "forge") {
        "forge"("net.minecraftforge:forge:${minecraft}-${mod.dep("forge_loader")}")
        mappings("net.fabricmc:yarn:$minecraft+build.${mod.dep("yarn_build")}:v2")

        annotationProcessor("io.github.llamalad7:mixinextras-common:${mod.dep("mixin_extras")}")?.let { compileOnly(it) }
        include("io.github.llamalad7:mixinextras-forge:${mod.dep("mixin_extras")}")?.let { implementation(it) }
    }
    if (loader == "neoforge") {
        "neoForge"("net.neoforged:neoforge:${mod.dep("neoforge_loader")}")
        mappings(loom.layered {
            mappings("net.fabricmc:yarn:$minecraft+build.${mod.dep("yarn_build")}:v2")
            mod.dep("neoforge_patch").takeUnless { it.startsWith('[') }?.let {
                mappings("dev.architectury:yarn-mappings-patch-neoforge:$it")
            }
        })
    }
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/$accessWidenerFile");

    decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }
    if(loader == "forge") {
        forge.mixinConfigs(
            "imaginebook-common.mixins.json",
            "imaginebook-forge.mixins.json",
        )
    }
}


val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
publishMods {
    val modrinthToken = localProperties.getProperty("publish.modrinthToken", "")
    val curseforgeToken = localProperties.getProperty("publish.curseforgeToken", "")


    file = project.tasks.remapJar.get().archiveFile
    dryRun = modrinthToken == null || curseforgeToken == null

    displayName =
        "${mod.name} ${loader.replaceFirstChar { it.uppercase() }} ${property("mod.mc_title")}-${mod.version}"
    version = mod.version
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = ReleaseType.STABLE

    modLoaders.add(loader)

    val targets = property("mod.mc_targets").toString().split(' ')
    modrinth {
        projectId = property("publish.modrinth").toString()
        accessToken = modrinthToken
        targets.forEach(minecraftVersions::add)
        if (loader == "fabric") {
            requires("fabric-api")
            optional("modmenu")
        }
    }

    curseforge {
        projectId = property("publish.curseforge").toString()
        accessToken = curseforgeToken.toString()
        targets.forEach(minecraftVersions::add)
        if (loader == "fabric") {
            requires("fabric-api")
            optional("modmenu")
        }
    }
}

java {
    withSourcesJar()
    val java = if (stonecutter.eval(minecraft, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
}

tasks.remapJar {
    injectAccessWidener = true
    input = tasks.shadowJar.get().archiveFile
    archiveClassifier = null
    atAccessWideners.add(accessWidenerFile)
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier = "dev"
}

tasks.processResources {
    properties(listOf("fabric.mod.json"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_dep_fabric")
    )
    properties(listOf("META-INF/mods.toml", "pack.mcmeta"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_dep_forgelike")
    )
    properties(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_dep_forgelike")
    )
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(buildAndCollect)
    }

    rootProject.tasks.register("runActive") {
        group = "project"
        dependsOn(tasks.named("runClient"))
    }
}
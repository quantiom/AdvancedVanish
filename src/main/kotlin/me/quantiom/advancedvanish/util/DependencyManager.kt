package me.quantiom.advancedvanish.util

import me.quantiom.advancedvanish.AdvancedVanish
import net.byteflux.libby.BukkitLibraryManager
import net.byteflux.libby.Library
import java.util.logging.Level

object DependencyManager {
    lateinit var libraryManager: BukkitLibraryManager

    private fun loadLibraries(vararg libraries: Library) {
        for (library in libraries) {
            this.libraryManager.loadLibrary(library)
        }
    }

    private fun library(groupId: String, artifactId: String, version: String, pattern: String, relocatePattern: String, repository: String = "https://repo1.maven.org/maven2/") =
        Library.builder()
            .groupId(groupId)
            .artifactId(artifactId)
            .version(version)
            .relocate(pattern, relocatePattern)
            .repository(repository)
            .build()

    fun loadDependencies(plugin: AdvancedVanish) {
        AdvancedVanish.log(Level.INFO, "Loading dependencies...")

        this.libraryManager = BukkitLibraryManager(plugin).apply {
            this.addMavenCentral()
        }

        // . has to be replaced with {} so maven doesn't relocate it here
        this.loadLibraries(
            library("org{}jetbrains{}exposed", "exposed-core", "0.39.2", "org{}jetbrains{}exposed", "me{}quantiom{}advancedvanish{}shaded{}exposed"),
            library("org{}jetbrains{}exposed", "exposed-dao", "0.39.2", "org{}jetbrains{}exposed", "me{}quantiom{}advancedvanish{}shaded{}exposed"),
            library("org{}jetbrains{}exposed", "exposed-jdbc", "0.39.2", "org{}jetbrains{}exposed", "me{}quantiom{}advancedvanish{}shaded{}exposed"),
            library("redis{}clients", "jedis", "4.2.0", "redis{}clients", "me{}quantiom{}advancedvanish{}shaded{}redis"),
            // adventure
            library("net{}kyori", "adventure-api", "4.11.0", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-platform-api", "4.1.2", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-platform-bukkit", "4.1.2", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-platform-facet", "4.1.2", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-text-serializer-bungeecord", "4.1.2", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-text-serializer-legacy", "4.11.0", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-nbt", "4.11.0", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-text-serializer-gson", "4.11.0", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-text-serializer-gson-legacy-impl", "4.11.0", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-platform-viaversion", "4.1.2", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-key", "4.11.0", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
            library("net{}kyori", "adventure-text-minimessage", "4.11.0", "net{}kyori{}adventure", "me{}quantiom{}advancedvanish{}shaded{}adventure"),
        )
    }
}
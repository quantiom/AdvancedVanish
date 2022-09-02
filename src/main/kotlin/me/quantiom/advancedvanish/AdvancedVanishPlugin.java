package me.quantiom.advancedvanish;

import me.quantiom.advancedvanish.util.DependencyManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is written in Java only because of the
 * dependency management system. After Kotlin is loaded, the
 * AdvancedVanish Kotlin class will take over.
 *
 * Basically, this class just acts as a "loader".
 */

public class AdvancedVanishPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // load dependencies
        DependencyManager dependencyManager = new DependencyManager(this);
        dependencyManager.loadDependencies();

        AdvancedVanish.INSTANCE.onEnable(this);
    }

    @Override
    public void onDisable() {
        AdvancedVanish.INSTANCE.onDisable();
    }
}

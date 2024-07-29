package mineway.dungeonsync.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigManager {

    private Plugin main;

    public ConfigManager(Plugin main) {
        this.main = main;
        createConfig("config");
    }

    public void createConfig(String file) {
        if (!(new File(main.getDataFolder(), file + ".yml")).exists()) {
            main.saveResource(file + ".yml", false);
        }
    }

    public FileConfiguration getConfig(String file) {
        File archive = new File(main.getDataFolder() + File.separator + file + ".yml");
        return YamlConfiguration.loadConfiguration(archive);
    }
}

package mineway.dungeonsync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mineway.dungeonsync.configuration.ConfigManager;
import mineway.dungeonsync.database.Database;
import mineway.dungeonsync.database.PlayerDAO;
import mineway.dungeonsync.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class DungeonSync extends JavaPlugin {

    private ConfigManager configManager;
    private Database database;
    private PlayerDAO playerDAO;

    @Override
    public void onEnable() {

        configManager = new ConfigManager(this);
        database = new Database(this, configManager, getLogger());
        playerDAO = new PlayerDAO(this, database, getLogger());

        getServer().getPluginManager().registerEvents(new PlayerListener(this, playerDAO), this);
    }

    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()){
            playerDAO.savePlayerData(player);
        }
        database.disconnect();
    }
}

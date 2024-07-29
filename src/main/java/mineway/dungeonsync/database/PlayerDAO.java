package mineway.dungeonsync.database;

import com.google.gson.Gson;
import mineway.dungeonsync.objects.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class PlayerDAO {

    private final String TABLE = "dungeonsync_players";
    private Plugin plugin;
    private Database database;
    private Logger logger;

    public PlayerDAO(Plugin plugin, Database database, Logger logger) {
        this.plugin = plugin;
        this.database = database;
        this.logger = logger;
        createTable();
        autoSave();
    }

    public void createTable() {
        CompletableFuture.runAsync(() -> {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE + "(" +
                    "uuid VARCHAR(100) NOT NULL PRIMARY KEY," +
                    "gamemode VARCHAR(20) NOT NULL," +
                    "totalexperience DOUBLE NOT NULL," +
                    "level INT NOT NULL," +
                    "exp FLOAT NOT NULL," +
                    "inventory LONGTEXT NOT NULL," +
                    "enderchest LONGTEXT NOT NULL," +
                    "maxhealth DOUBLE NOT NULL," +
                    "health DOUBLE NOT NULL," +
                    "ishealthscaled BOOL NOT NULL," +
                    "healthscale DOUBLE NOT NULL," +
                    "food INT NOT NULL," +
                    "saturation FLOAT NOT NULL," +
                    "helditemslot INT NOT NULL" +
                    ");";
            try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
                statement.execute();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public PlayerData getPlayerData(UUID uuid) {
        String sql = "SELECT * FROM `"+TABLE+"` WHERE uuid = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.execute();
            if(statement.getResultSet().next()) {
                logger.info("Player '" + uuid + "' is loaded of DataBase.");
                return new PlayerData(uuid,
                        GameMode.valueOf(statement.getResultSet().getString(2)),
                        statement.getResultSet().getInt(3),
                        statement.getResultSet().getInt(4),
                        statement.getResultSet().getFloat(5),
                        statement.getResultSet().getString(6),
                        statement.getResultSet().getString(7),
                        statement.getResultSet().getDouble(8),
                        statement.getResultSet().getDouble(9),
                        statement.getResultSet().getBoolean(10),
                        statement.getResultSet().getDouble(11),
                        statement.getResultSet().getInt(12),
                        statement.getResultSet().getFloat(13),
                        statement.getResultSet().getInt(14));
            }
            return null;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void asyncSavePlayerData(Player player){
        CompletableFuture.runAsync(() -> savePlayerData(player));
    }

    public void savePlayerData(Player player){
        String sql = "REPLACE INTO " + TABLE + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            PlayerData playerData = new PlayerData(player);
            statement.setString(1, playerData.getUuid().toString());
            statement.setString(2, playerData.getGameMode().name());
            statement.setInt(3, playerData.getTotalExperience());
            statement.setInt(4, playerData.getLevel());
            statement.setFloat(5, playerData.getExp());
            statement.setString(6, playerData.getInventory());
            statement.setString(7, playerData.getEnderChest());
            statement.setDouble(8, playerData.getMaxHealth());
            statement.setDouble(9, playerData.getHealth());
            statement.setBoolean(10, playerData.isHealthScaled());
            statement.setDouble(11, playerData.getHealthScale());
            statement.setInt(12, playerData.getFoodLevel());
            statement.setFloat(13, playerData.getSaturation());
            statement.setInt(14, playerData.getHeldItemSlot());
            statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void autoSave(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : plugin.getServer().getOnlinePlayers()){
                    asyncSavePlayerData(player);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60);
    }

}

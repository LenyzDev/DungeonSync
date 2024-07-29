package mineway.dungeonsync.database;

import mineway.dungeonsync.configuration.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Database {
    private Connection connection;
    private ConfigManager configManager;
    private Logger logger;

    public Database(Plugin plugin, ConfigManager configManager, Logger logger){
        this.configManager = configManager;
        this.logger = logger;
        connect();
        heartbeat(plugin);
    }

    public Connection connect() {
        FileConfiguration config = configManager.getConfig("config");
        String address = config.getString("MySQL.ip");
        String database = config.getString("MySQL.database");
        String username = config.getString("MySQL.username");
        String password = config.getString("MySQL.password");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://"+address+"/"+database, username, password);
            logger.info("has connected in MySql Database.");
            return connection;

        }catch (Exception e){
            e.printStackTrace();
            logger.severe("don't connect in MySql database.");
            return null;
        }
    }

    private void heartbeat(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    connection.createStatement().execute("SELECT 1");
                    logger.info("heartbeat executed.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.severe("heartbeat failed.");
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60 * 60, 20 * 60 * 60);
    }

    public void disconnect(){
        if(connection != null){
            try {
                logger.info("has disconnected from MySql Database.");
                connection.close();
            } catch (SQLException e){
                e.printStackTrace();
                logger.severe("don't disconnect from MySql database.");
            }
        }
    }

    public Connection getConnection(){
        try {
            if (connection == null || connection.isClosed()) {
                return connect();
            }

            return connection;

        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}

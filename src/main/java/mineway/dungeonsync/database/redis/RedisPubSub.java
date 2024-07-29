package mineway.dungeonsync.database.redis;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.logging.Logger;

public class RedisPubSub {

    private Plugin plugin;
    private Map<String, String> playerDAO;
    private JedisPool jedisPool;

    public RedisPubSub(Plugin plugin, FileConfiguration config, Logger logger) {
        logger.warning("Connecting to Redis...");
        this.plugin = plugin;
        this.jedisPool = new JedisPool(
                buildPoolConfig(100),
                config.getString("redis.host"),
                config.getInt("redis.port"),
                500,
                config.getString("redis.password"));
        logger.warning("Redis Connected.");
        startMessageListener(logger);
    }

    public void sendMessage(String playerName, String messageId) {
        getJedis().publish("premiumMessage", playerName+";"+messageId);
    }

    private void startMessageListener(Logger logger) {
        logger.warning("Starting Redis Listener...");
        new Thread(() -> {
            JedisPubSub jedisPubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String messageId) {
                    if (!plugin.isEnabled()) return;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String[] values = messageId.split(";");
                        if(values.length != 2) {
                            return;
                        }
                        String nick = values[0];
                        int months;
                        try {
                            months = Integer.parseInt(values[1]);
                        } catch (NumberFormatException e) {
                            return;
                        }
                        System.out.println("("+channel+") Received message: ("+nick+") " + months);
                    });
                }
            };
            getJedis().subscribe(jedisPubSub, "premiumMessage");
        }).start();
        logger.warning("Redis Listener Started.");
    }

    protected JedisPoolConfig buildPoolConfig(int maxConnections) {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxConnections);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
    }

    public Jedis getJedis() {
        return this.jedisPool.getResource();
    }

    public void close() {
        this.jedisPool.close();
    }
}

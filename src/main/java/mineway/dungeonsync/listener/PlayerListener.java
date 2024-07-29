package mineway.dungeonsync.listener;

import com.google.gson.Gson;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import mineway.dungeonsync.database.PlayerDAO;
import mineway.dungeonsync.objects.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private Plugin plugin;
    private PlayerDAO playerDAO;
    private Map<String, Boolean> savingPlayers;
    private Map<String, Boolean> lockedPlayers;

    public PlayerListener(Plugin plugin, PlayerDAO playerDAO) {
        this.plugin = plugin;
        this.playerDAO = playerDAO;
        this.savingPlayers = new HashMap<>();
        this.lockedPlayers = new HashMap<>();
        autoSave();
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        lockPlayer(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline()) return;
                PlayerData playerData = playerDAO.getPlayerData(player.getUniqueId());
                if(playerData == null) {
                    plugin.getLogger().info("Player data not found for " + player.getName());
                    unlockPlayer(player.getUniqueId());
                    return;
                }
                lockedPlayers.remove(player.getUniqueId().toString());
                playerData.apply(player);
                plugin.getLogger().info("Player data loaded for " + player.getName());
            }
        }.runTaskLater(plugin, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(lockedPlayers.containsKey(player.getUniqueId().toString())) return;
        playerDAO.savePlayerData(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        playerDAO.savePlayerData(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if(!isLocked(player.getUniqueId())) {
            savingPlayers.put(player.getUniqueId().toString(), true);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(!isLocked(player.getUniqueId())) {
            savingPlayers.put(player.getUniqueId().toString(), true);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(!isLocked(player.getUniqueId())) {
            savingPlayers.put(player.getUniqueId().toString(), true);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if(!isLocked(player.getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(!isLocked(player.getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerClick(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if(!isLocked(player.getUniqueId())) {
            savingPlayers.put(player.getUniqueId().toString(), true);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if(!isLocked(player.getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        if(!isLocked(player.getUniqueId())) {
            savingPlayers.put(player.getUniqueId().toString(), true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerTradeEvent event) {
        Player player = event.getPlayer();
        if(!isLocked(player.getUniqueId())) {
            savingPlayers.put(player.getUniqueId().toString(), true);
            return;
        }
        event.setCancelled(true);
    }

    public boolean isLocked(UUID uuid) {
        return lockedPlayers.containsKey(uuid.toString());
    }

    public void lockPlayer(UUID uuid) {
        lockedPlayers.put(uuid.toString(), true);
    }

    public void unlockPlayer(UUID uuid) {
        lockedPlayers.remove(uuid.toString());
    }

    private void autoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String uuid : new ArrayList<>(savingPlayers.keySet())) {
                    Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                    if(player == null) {
                        savingPlayers.remove(uuid);
                        continue;
                    }
                    playerDAO.savePlayerData(player);
                    savingPlayers.remove(uuid);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 10, 20 * 10);
    }
}

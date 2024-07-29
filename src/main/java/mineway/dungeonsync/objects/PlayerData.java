package mineway.dungeonsync.objects;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private GameMode gameMode;
    private int totalExperience;
    private int level;
    private float exp;
    private String inventory;
    private String enderChest;
    private double maxHealth;
    private double health;
    private boolean isHealthScaled;
    private double healthScale;
    private int foodLevel;
    private float saturation;
    private int heldItemSlot;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.gameMode = player.getGameMode();
        this.totalExperience = player.getTotalExperience();
        this.level = player.getLevel();
        this.exp = player.getExp();
        ItemStack[] inventoryContents = player.getInventory().getContents();
        this.inventory = NBT.itemStackArrayToNBT(inventoryContents).toString();
        ItemStack[] enderChestContents = player.getEnderChest().getContents();
        this.enderChest = NBT.itemStackArrayToNBT(enderChestContents).toString();
        this.maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        this.health = player.getHealth();
        this.isHealthScaled = player.isHealthScaled();
        this.healthScale = player.getHealthScale();
        this.foodLevel = player.getFoodLevel();
        this.saturation = player.getSaturation();
        this.heldItemSlot = player.getInventory().getHeldItemSlot();
    }

    public PlayerData(UUID uuid, GameMode gameMode, int totalExperience, int level, float exp, String inventory, String enderChest, double maxHealth, double health, boolean isHealthScaled, double healthScale, int foodLevel, float saturation, int heldItemSlot) {
        this.uuid = uuid;
        this.gameMode = gameMode;
        this.totalExperience = totalExperience;
        this.level = level;
        this.exp = exp;
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.maxHealth = maxHealth;
        this.health = health;
        this.isHealthScaled = isHealthScaled;
        this.healthScale = healthScale;
        this.foodLevel = foodLevel;
        this.saturation = saturation;
        this.heldItemSlot = heldItemSlot;
    }

    public PlayerData(String data){
        String[] split = data.split(";:!:;");
        this.uuid = UUID.fromString(split[0]);
        this.gameMode = GameMode.valueOf(split[1]);
        this.totalExperience = Integer.parseInt(split[2]);
        this.level = Integer.parseInt(split[3]);
        this.exp = Float.parseFloat(split[4]);
        this.inventory = split[5];
        this.enderChest = split[6];
        this.maxHealth = Double.parseDouble(split[7]);
        this.health = Double.parseDouble(split[8]);
        this.isHealthScaled = Boolean.parseBoolean(split[9]);
        this.healthScale = Double.parseDouble(split[10]);
        this.foodLevel = Integer.parseInt(split[11]);
        this.saturation = Float.parseFloat(split[12]);
        this.heldItemSlot = Integer.parseInt(split[13]);
    }

    public String toString() {
        return uuid.toString() + ";:!:;" +
                gameMode + ";:!:;" +
                totalExperience + ";:!:;" +
                level + ";:!:;" +
                exp + ";:!:;" +
                inventory + ";:!:;" +
                enderChest + ";:!:;" +
                maxHealth + ";:!:;" +
                health + ";:!:;" +
                isHealthScaled + ";:!:;" +
                healthScale + ";:!:;" +
                foodLevel + ";:!:;" +
                saturation + ";:!:;" +
                heldItemSlot;
    }

    public void apply(Player player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.maxHealth);
        player.setGameMode(this.gameMode);
        player.setTotalExperience(this.totalExperience);
        player.setLevel(this.level);
        player.setExp(this.exp);
        ItemStack[] inventoryContents = NBT.itemStackArrayFromNBT(NBT.parseNBT(this.inventory));
        player.getInventory().setContents(inventoryContents);
        ItemStack[] enderChestContents = NBT.itemStackArrayFromNBT(NBT.parseNBT(this.enderChest));
        player.getEnderChest().setContents(enderChestContents);
        player.setHealthScaled(this.isHealthScaled);
        player.setHealthScale(this.healthScale);
        player.setFoodLevel(this.foodLevel);
        player.setSaturation(this.saturation);
        player.getInventory().setHeldItemSlot(this.heldItemSlot);
        if(player.isDead()){
            player.spigot().respawn();
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public int getLevel() {
        return level;
    }

    public float getExp() {
        return exp;
    }

    public String getInventory() {
        return inventory;
    }

    public String getEnderChest() {
        return enderChest;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getHealth() {
        return health;
    }

    public boolean isHealthScaled() {
        return isHealthScaled;
    }

    public double getHealthScale() {
        return healthScale;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public float getSaturation() {
        return saturation;
    }

    public int getHeldItemSlot() {
        return heldItemSlot;
    }

}

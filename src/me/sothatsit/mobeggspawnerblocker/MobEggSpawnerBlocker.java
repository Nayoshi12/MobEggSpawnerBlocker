package me.sothatsit.mobeggspawnerblocker;


import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MobEggSpawnerBlocker extends JavaPlugin implements Listener {

    private String message;
    private boolean blockCreative;
    //Server is enabled register Player Interact Listener
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        reloadConfiguration();
    }

    public void reloadConfiguration() {
        //Initialize and reinitialize the config and all the loggers
        this.saveDefaultConfig();
        this.reloadConfig();

        if (!getConfig().isSet("message") || !getConfig().isString("message")) {
            getLogger().warning("\"message\" not set or invalid in config, resetting to default");
            getConfig().set("message", "&cChanging spawners using mob eggs is disabled on this server");
            saveConfig();
        }

        if (!getConfig().isSet("block-creative") || !getConfig().isBoolean("block-creative")) {
            getLogger().warning("\"block-creative\" not set or invalid in config, resetting to default");
            getConfig().set("block-creative", false);
            saveConfig();
        }

        this.message = getConfig().getString("message");
        this.blockCreative = getConfig().getBoolean("block-creative");
    }

    void print(String str) {
        System.out.println(str);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        //Checks if the player is in creative
        if (!blockCreative && e.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        //Checks if the player is Opped and has permissions
        if (e.getPlayer().isOp() || e.getPlayer().hasPermission("mobeggspawnerblocker.override"))
            return;
        //Checks if the player is destroying the monster spawner or not
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Block b = e.getClickedBlock();

        //Check if the clicked block is an air or null
        if (b == null)
            return;
        //Check if the block is a spawner
        if (b.getType() != Material.SPAWNER)
            return;

        ItemStack i = e.getItem();
        //check if the held item is null
        if (i == null)
            return;
        //check if the item is a spawn egg or not
        if (!(i.getType().toString().contains("SPAWN_EGG")))
            return;

        CreatureSpawner cs = (CreatureSpawner) b.getState();
        //Get Locations
        final Location loc = cs.getLocation();
        final EntityType type = cs.getSpawnedType();

        if (message != null && !message.isEmpty())
            //Get the message from config for full configuration
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',message)));
        e.setCancelled(true);

        //Get block location and cxheck if the block is a creature spawner and if it comes down to here, it will change the spawned type
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Block b = loc.getBlock();

                if (b == null || b.getType() != Material.SPAWNER)
                    return;

                CreatureSpawner cs = (CreatureSpawner) b.getState();

                cs.setSpawnedType(type);
            }
        });
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //Command: /mobeggspawnerblocker: To reload
        if (label.equalsIgnoreCase("mobeggspawnerblocker")) {
            if (!sender.isOp() && !sender.hasPermission("mobeggspawnerblocker.reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Error > &cYou do not have permission to run this command."));
                return true;
            }

            reloadConfiguration();
            sender.sendMessage(ChatColor.GREEN + "MobEggSpawnerBlocker config reloaded");

            return true;
        }

        return false;
    }
}
package com.azpbmd.privatecreative;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public class EventListener implements Listener {

    private final DataManager dataManager = PrivateCreative.getInstance().getDataManager();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Always teleport to own world on join
        WorldManager.teleportToWorld(player, playerUUID);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Unload if now empty (after quit)
        Bukkit.getScheduler().runTaskLater(PrivateCreative.getInstance(), () -> WorldManager.unloadIfEmpty(world), 20L);  // Delay to ensure quit processed
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        World world = player.getWorld();
        String worldName = world.getName();

        // Only enforce in private worlds
        if (!worldName.endsWith("_world")) return;

        WorldBorder border = world.getWorldBorder();
        Location to = event.getTo();

        // If moving outside border, snap back to spawn (safe center)
        if (!border.isInside(to)) {
            Location spawn = world.getSpawnLocation();  // 0,100,0
            event.setTo(spawn);  // Prevent the bad move entirely
            player.sendMessage(ChatColor.RED + "You cannot go beyond the border! Snapped back to spawn.");
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Set respawn location to private world spawn
        World privateWorld = WorldManager.getOrCreateWorld(playerUUID);
        Location spawn = privateWorld.getSpawnLocation();  // 0,100,0 in private world
        event.setRespawnLocation(spawn);
        player.setGameMode(GameMode.CREATIVE);  // Ensure creative mode on respawn
    }
}

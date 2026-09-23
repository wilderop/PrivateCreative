package com.azpbmd.privatecreative;

import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WorldManager {

    /**
     * Get or create a player's private world.
     * @param playerUUID The player's UUID
     * @return The World object (loaded or created)
     */
    public static World getOrCreateWorld(UUID playerUUID) {
        String worldName = playerUUID.toString() + "_world";
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            // Create new world
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.FLAT);
            // Classic superflat: bedrock1 + dirt3 + grass1, plains, NO STRUCTURES
            creator.generatorSettings("{\"layers\":[{\"block\":\"minecraft:bedrock\",\"height\":1},{\"block\":\"minecraft:dirt\",\"height\":3},{\"block\":\"minecraft:grass_block\",\"height\":1}],\"biome\":\"minecraft:plains\"}");
            creator.generateStructures(false);  // Disable ALL structures (trial chambers, etc.)
            world = creator.createWorld();

            // Set spawn point
            Location spawn = new Location(world, 0, 100, 0);
            world.setSpawnLocation(spawn);

            // Set world border
            updateBorder(world, playerUUID);

            // Disable mob spawning, weather, etc. for creative focus
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        return world;
    }

    /**
     * Update the world border based on current radius.
     */
    public static void updateBorder(World world, UUID ownerUUID) {
        WorldBorder border = world.getWorldBorder();
        int radius = PrivateCreative.getInstance().getDataManager().getRadius(ownerUUID);
        border.setCenter(0, 0);  // Center on spawn (y ignored for border)
        border.setSize(2 * radius + 1);  // Diameter slightly over to include edges
        border.setDamageAmount(0);  // No damage, just teleport back
        border.setWarningDistance(0);  // No warning
    }

    /**
     * Unload a world if empty.
     * @param world The world to check/unload
     */
    public static void unloadIfEmpty(World world) {
        if (world.getPlayers().isEmpty()) {
            Bukkit.unloadWorld(world, true);  // Save changes
        }
    }

    /**
     * Teleport player to a world, set creative, and handle loading.
     * @param player The player to teleport
     * @param targetUUID The owner of the target world
     */
    public static void teleportToWorld(Player player, UUID targetUUID) {
        World targetWorld = getOrCreateWorld(targetUUID);
        Location spawn = targetWorld.getSpawnLocation();
        player.teleport(spawn);
        player.setGameMode(GameMode.CREATIVE);
    }
}

package com.azpbmd.privatecreative;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {

    private final File dataFile;
    private FileConfiguration dataConfig;

    // In-memory caches for quick access
    private final Map<UUID, Integer> playerRadii = new HashMap<>();
    private final Map<UUID, Set<UUID>> invites = new HashMap<>();  // Key: owner UUID, Value: Set of invited UUIDs

    public DataManager() {
        dataFile = new File(PrivateCreative.getInstance().getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadData();
    }

    private void loadData() {
        // Load radii
        if (dataConfig.contains("radii")) {
            for (String uuidStr : dataConfig.getConfigurationSection("radii").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                int radius = dataConfig.getInt("radii." + uuidStr);
                playerRadii.put(uuid, radius);
            }
        }

        // Load invites
        if (dataConfig.contains("invites")) {
            for (String ownerStr : dataConfig.getConfigurationSection("invites").getKeys(false)) {
                UUID owner = UUID.fromString(ownerStr);
                List<String> invitedStrs = dataConfig.getStringList("invites." + ownerStr);
                Set<UUID> invited = new HashSet<>();
                for (String invitedStr : invitedStrs) {
                    invited.add(UUID.fromString(invitedStr));
                }
                invites.put(owner, invited);
            }
        }
    }

    public void saveData() {
        // Save radii
        for (Map.Entry<UUID, Integer> entry : playerRadii.entrySet()) {
            dataConfig.set("radii." + entry.getKey().toString(), entry.getValue());
        }

        // Save invites
        for (Map.Entry<UUID, Set<UUID>> entry : invites.entrySet()) {
            List<String> invitedStrs = new ArrayList<>();
            for (UUID invited : entry.getValue()) {
                invitedStrs.add(invited.toString());
            }
            dataConfig.set("invites." + entry.getKey().toString(), invitedStrs);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRadius(UUID playerUUID) {
        return playerRadii.getOrDefault(playerUUID, PrivateCreative.getInstance().getConfig().getInt("default-radius", 250));
    }

    public void setRadius(UUID playerUUID, int radius) {
        playerRadii.put(playerUUID, radius);
        saveData();
    }

    public Set<UUID> getInvited(UUID ownerUUID) {
        return invites.computeIfAbsent(ownerUUID, k -> new HashSet<>());
    }

    public void addInvite(UUID ownerUUID, UUID invitedUUID) {
        getInvited(ownerUUID).add(invitedUUID);
        saveData();
    }

    public void removeInvite(UUID ownerUUID, UUID invitedUUID) {
        getInvited(ownerUUID).remove(invitedUUID);
        saveData();
    }

    public boolean isInvited(UUID ownerUUID, UUID visitorUUID) {
        if (ownerUUID.equals(visitorUUID)) return true;  // Always allowed in own world
        return getInvited(ownerUUID).contains(visitorUUID);
    }

    public Set<UUID> getVisitList(UUID visitorUUID) {
        Set<UUID> visitable = new HashSet<>();
        for (Map.Entry<UUID, Set<UUID>> entry : invites.entrySet()) {
            if (entry.getValue().contains(visitorUUID)) {
                visitable.add(entry.getKey());
            }
        }
        visitable.add(visitorUUID);  // Include own world
        return visitable;
    }
}

package com.azpbmd.privatecreative;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import org.bukkit.World;
public class CommandExecutor implements org.bukkit.command.CommandExecutor, TabExecutor {

    private final DataManager dataManager = PrivateCreative.getInstance().getDataManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        switch (label.toLowerCase()) {
            case "home":
                WorldManager.teleportToWorld(player, playerUUID);
                player.sendMessage(ChatColor.GREEN + "Teleported to your home world.");
                return true;

            case "invite":
                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /invite <player>");
                    return true;
                }
                Player invitee = Bukkit.getPlayer(args[0]);
                UUID inviteeUUID = (invitee != null) ? invitee.getUniqueId() : Bukkit.getOfflinePlayer(args[0]).getUniqueId();
                if (inviteeUUID == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                dataManager.addInvite(playerUUID, inviteeUUID);
                player.sendMessage(ChatColor.GREEN + "Invited " + args[0] + " to your world.");
                return true;

            case "banish":
                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /banish <player>");
                    return true;
                }
                Player banished = Bukkit.getPlayer(args[0]);
                UUID banishedUUID = (banished != null) ? banished.getUniqueId() : Bukkit.getOfflinePlayer(args[0]).getUniqueId();
                if (banishedUUID == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                dataManager.removeInvite(playerUUID, banishedUUID);
                player.sendMessage(ChatColor.GREEN + "Banished " + args[0] + " from your world.");

                // If online and in your world, kick them back
                if (banished != null && banished.getWorld().getName().equals(playerUUID + "_world")) {
                    WorldManager.teleportToWorld(banished, banishedUUID);
                    banished.sendMessage(ChatColor.RED + "You have been banished from " + player.getName() + "'s world.");
                }
                return true;

            case "visit":
                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /visit <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                UUID targetUUID = (target != null) ? target.getUniqueId() : Bukkit.getOfflinePlayer(args[0]).getUniqueId();
                if (targetUUID == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if (!dataManager.isInvited(targetUUID, playerUUID)) {
                    player.sendMessage(ChatColor.RED + "You are not invited to " + args[0] + "'s world.");
                    return true;
                }
                WorldManager.teleportToWorld(player, targetUUID);
                player.sendMessage(ChatColor.GREEN + "Visiting " + args[0] + "'s world.");
                return true;

            case "visitlist":
                Set<UUID> visitable = dataManager.getVisitList(playerUUID);
                player.sendMessage(ChatColor.GREEN + "Worlds you can visit:");
                for (UUID owner : visitable) {
                    String name = Bukkit.getOfflinePlayer(owner).getName();
                    player.sendMessage("- " + name);
                }
                return true;

            case "invitelist":
                Set<UUID> invited = dataManager.getInvited(playerUUID);
                player.sendMessage(ChatColor.GREEN + "Players invited to your world:");
                for (UUID inv : invited) {
                    String name = Bukkit.getOfflinePlayer(inv).getName();
                    player.sendMessage("- " + name);
                }
                return true;

            case "pworld":
                if (!player.isOp()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission.");
                    return true;
                }
                if (args.length < 3 || !args[0].equalsIgnoreCase("expand")) {
                    player.sendMessage(ChatColor.RED + "Usage: /pworld expand <player> <new_radius>");
                    return true;
                }
                Player expandTarget = Bukkit.getPlayer(args[1]);
                UUID expandUUID = (expandTarget != null) ? expandTarget.getUniqueId() : Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                if (expandUUID == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                int newRadius;
                try {
                    newRadius = Integer.parseInt(args[2]);
                    if (newRadius < 1) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid radius (must be positive integer).");
                    return true;
                }
                dataManager.setRadius(expandUUID, newRadius);
                World expandWorld = Bukkit.getWorld(expandUUID + "_world");
                if (expandWorld != null) {
                    WorldManager.updateBorder(expandWorld, expandUUID);
                }
                player.sendMessage(ChatColor.GREEN + "Updated " + args[1] + "'s radius to " + newRadius + ".");
                PrivateCreative.getInstance().getLogger().info(player.getName() + " updated " + args[1] + "'s radius to " + newRadius);
                return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Basic tab completion for player names
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && (alias.equalsIgnoreCase("invite") || alias.equalsIgnoreCase("banish") || alias.equalsIgnoreCase("visit") || (alias.equalsIgnoreCase("pworld") && args[0].equalsIgnoreCase("expand") && args.length == 2))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        } else if (alias.equalsIgnoreCase("pworld") && args.length == 1) {
            completions.add("expand");
        }
        return completions;
    }
}

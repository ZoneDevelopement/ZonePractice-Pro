package dev.nandi0813.practice.command.event.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum SpawnPointArg {
    ;

    public static void spawnPointCommand(Player player, String label1, EventData eventData, String[] args) {
        if (args.length == 3) {
            try {
                switch (args[2]) {
                    case "add":
                        eventData.addSpawn(player.getLocation());
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SPAWN-POSITION.SPAWN-ADDED")
                                .replace("%event%", eventData.getType().name())
                                .replace("%posCount%", String.valueOf(eventData.getSpawns().size())));
                        break;
                    case "remove":
                        eventData.removeSpawn(player.getLocation());
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SPAWN-POSITION.SPAWN-REMOVED")
                                .replace("%event%", eventData.getType().name())
                                .replace("%posCount%", String.valueOf(eventData.getSpawns().size())));
                        break;
                    case "clear":
                        eventData.clearSpawn();
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SPAWN-POSITION.SPAWN-CLEARED")
                                .replace("%event%", eventData.getType().name()));
                        break;
                    case "list":
                        if (eventData.getSpawns().isEmpty()) {
                            Common.sendMMMessage(player, "<red>No spawn points found.");
                        } else {
                            int i = 1;
                            for (Location location : eventData.getSpawns()) {
                                Common.sendMMMessage(player, "<gold>[" + i + "]: <gray>" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
                                i++;

                                Common.sendMMMessage(player,
                                        " <red>» <click:run_command:'/tp " + player.getName() + " "
                                                + location.getBlockX() + " "
                                                + location.getBlockY() + " "
                                                + location.getBlockZ() + "'><hover:show_text:'<gray>Teleport to this position.'>Click to teleport</hover></click>");
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                Common.sendMMMessage(player, "<red>" + e.getMessage());
            }
        }
        // No help message - spawn commands work silently or show error
    }

}

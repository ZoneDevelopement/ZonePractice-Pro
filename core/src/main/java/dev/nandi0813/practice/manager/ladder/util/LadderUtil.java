package dev.nandi0813.practice.manager.ladder.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.backend.MysqlManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.type.FireballFight;
import dev.nandi0813.practice.manager.ladder.type.SkyWars;
import dev.nandi0813.practice.manager.leaderboard.LeaderboardManager;
import dev.nandi0813.practice.manager.leaderboard.types.LbMainType;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;

public enum LadderUtil {
    ;

    public static void changeStatus(Player player, NormalLadder ladder) {
        if (!ladder.isEnabled()) {
            if (ladder.getIcon() == null) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.NO-ICON"));
                return;
            }
            if (!ladder.getKitData().isSet()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.NO-CONTENT"));
                return;
            }
            if (ladder instanceof SkyWars && ((SkyWars) ladder).getSkyWarsLoot() == null) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.SKYWARS.NO-LOOT"));
                return;
            }

            enableLadder(ladder);
        } else {
            if (!MatchManager.getInstance().getLiveMatchesByLadder(ladder).isEmpty()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.CANT-DISABLE"));
                return;
            }

            disableLadder(ladder);
        }
    }

    public static void disableLadder(NormalLadder ladder) {
        ladder.setEnabled(false);
        ladder.setFrozen(false);

        // Remove the ladder from the arenas and holograms.
        ArenaManager.getInstance().removeLadder(ladder);
        LeaderboardManager.getInstance().removeLadder(ladder);

        /*
         * Update GUIs
         */
        LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).update();
        GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();
        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();

        GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update(true);
        GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update(true);
        GUIManager.getInstance().searchGUI(GUIType.CustomLadder_Selector).update();

        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                profile.getFile().deleteCustomKit(ladder);
                profile.getUnrankedCustomKits().remove(ladder);
                profile.getRankedCustomKits().remove(ladder);
            }
        });

        /*
         * Delete the ladder statistics from the mysql table.
         */
        MysqlManager.deleteLadderStatsAsync(ladder.getName());
    }

    public static void enableLadder(NormalLadder ladder) {
        ladder.setEnabled(true);

        if (!ladder.getPreviouslyAssignedArenas().isEmpty()) {
            for (String arenaName : new HashSet<>(ladder.getPreviouslyAssignedArenas())) {
                // Try normal arena first
                Arena arena = ArenaManager.getInstance().getNormalArena(arenaName);
                if (arena != null) {
                    if (ladder.getPreviouslyAssignedArenas().contains(arena.getName()) &&
                        arena.getAssignedLadderTypes().contains(ladder.getType())
                    ) {
                        arena.getAssignedLadders().add(ladder);
                    }
                } else {
                    // Try FFA arena if normal arena not found
                    FFAArena ffaArena = ArenaManager.getInstance().getFFAArena(arenaName);
                    if (ffaArena != null) {
                        if (ladder.getPreviouslyAssignedArenas().contains(ffaArena.getName())) {
                            ffaArena.getAssignedLadders().add(ladder);
                        }
                    }
                }
            }
            ladder.getPreviouslyAssignedArenas().clear();
        }

        // Update GUIs
        ladder.getPreviewGui().update();
        GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update(true);
        GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update(true);
        GUIManager.getInstance().searchGUI(GUIType.CustomLadder_Selector).update();

        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            /*
            for (Arena arena : ArenaManager.getInstance().getNormalArenas()) {
                if (arena.getAssignedLadderTypes().contains(ladder.getType()))
                    arena.getAssignedLadders().add(ladder);
            }
             */

            // Update ladder setup GUIs
            for (Map<GUIType, GUI> map : HologramSetupManager.getInstance().getHologramSetupGUIs().values())
                map.get(GUIType.Hologram_Ladder).update();
            for (Map<GUIType, GUI> map : ArenaGUISetupManager.getInstance().getArenaSetupGUIs().values()) {
                if (map.containsKey(GUIType.Arena_Ladders_Type))
                    map.get(GUIType.Arena_Ladders_Type).update();

                map.get(GUIType.Arena_Ladders_Single).update();
            }

            LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).update();
            GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();

            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                profile.getStats().createLadderStat(ladder);

                // Set the custom ladder kits
                if (ladder.isEditable()) {
                    profile.getUnrankedCustomKits().put(ladder, new HashMap<>());

                    if (ladder.isRanked()) {
                        profile.getRankedCustomKits().put(ladder, new HashMap<>());
                    }
                }
            }

            // Update ladder leaderboard
            for (LbMainType lbMainType : LbMainType.values()) {
                for (LbSecondaryType lbSecondaryType : LbSecondaryType.values()) {
                    if (lbMainType.equals(LbMainType.LADDER)) {
                        LeaderboardManager.getInstance().updateLB(lbMainType, lbSecondaryType, ladder);
                    }
                }
            }
        });
    }

    public static Arena getAvailableArena(Ladder ladder) {
        List<Arena> availableArenas = ladder.getAvailableArenas();

        if (!availableArenas.isEmpty())
            return availableArenas.get(new Random().nextInt(availableArenas.size()));
        else
            return null;
    }

    public static void loadInventory(Player player, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra) {
        player.getInventory().setArmorContents(armor);
        player.getInventory().setStorageContents(inventory);
        player.getInventory().setExtraContents(extra);
    }

    private static final String[] MATERIAL_TYPES = {
            "_WOOL", "_STAINED_CLAY", "_STAINED_GLASS", "_STAINED_GLASS_PANE", "_CARPET",
            "_CONCRETE", "_CONCRETE_POWDER", "_TERRACOTTA", "_GLAZED_TERRACOTTA", "_CANDLE", "_BANNER"
    };

    public static ItemStack changeItemColor(@NotNull ItemStack item, Component teamColor) {
        String itemType = item.getType().toString();
        TextColor textColor = teamColor.color();
        Color color = Color.YELLOW;
        if (textColor != null) {
            color = Color.fromRGB(
                    Objects.requireNonNull(teamColor.color()).red(),
                    Objects.requireNonNull(teamColor.color()).green(),
                    Objects.requireNonNull(teamColor.color()).blue()
            );
        }

        if (item.getType().name().startsWith("LEATHER_")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            if (meta != null) {
                meta.setColor(color);
                item.setItemMeta(meta);
            }
            return item;
        }

        for (String type : MATERIAL_TYPES) {
            if (itemType.contains(type) && textColor != null) {
                try {
                    Material material = Material.getMaterial(textColor.toString().toUpperCase() + type);

                    if (material != null) {
                        return item.withType(material);
                    }
                } catch (Exception ignored) {
                    break;
                }
            }
        }

        return item;
    }

    public static ItemStack getPotionItem(String string) {
        try {
            if (string.contains("::")) {
                String[] split = string.split("::");
                ItemStack itemStack = new ItemStack(Material.valueOf(split[0]));

                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                if (potionMeta != null)
                    potionMeta.setBasePotionType(PotionType.valueOf(split[1]));

                itemStack.setItemMeta(potionMeta);
                return itemStack;
            }
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Invalid item: " + string);
        }
        return null;
    }

    public static boolean isUnbreakable(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            return item.getItemMeta().isUnbreakable();
        }
        return false;
    }

    public static ItemMeta setUnbreakable(ItemMeta itemMeta, boolean unbreakable) {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(unbreakable);
        }
        return itemMeta;
    }

    public static ItemStack setDurability(ItemStack itemStack, int durability) {
        if (itemStack.getItemMeta() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof Damageable damageable) {
                int newDamage = itemStack.getType().getMaxDurability() - durability;
                if (newDamage < 0 || newDamage > itemStack.getType().getMaxDurability()) {
                    newDamage = itemStack.getType().getMaxDurability();
                }

                damageable.setDamage(newDamage);
                itemStack.setItemMeta(damageable);
                return itemStack;
            }
        }
        return itemStack;
    }

    public static void placeTnt(BlockPlaceEvent e, Match match) {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (e.isCancelled()) {
                return;
            }

            Block block = e.getBlock();
            block.setBlockData(Material.AIR.createBlockData());
            block.getState().update();

            TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation().subtract(-0.5, 0, -0.5), EntityType.TNT);
            BlockUtil.setMetadata(tnt, FIGHT_ENTITY, match);
            tnt.setIsIncendiary(false);

            if (match.getLadder() instanceof FireballFight) {
                BlockUtil.setMetadata(tnt, FireballFight.FIREBALL_FIGHT_TNT, match);
                BlockUtil.setMetadata(tnt, FireballFight.FIREBALL_FIGHT_TNT_SHOOTER, e.getPlayer());
            }
        }, 2L);
    }

}

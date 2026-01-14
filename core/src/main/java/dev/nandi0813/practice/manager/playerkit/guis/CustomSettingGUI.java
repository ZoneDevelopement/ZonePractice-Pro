package dev.nandi0813.practice.manager.playerkit.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.ladder.enums.KnockbackType;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.playerkit.StaticItems;
import dev.nandi0813.practice.util.InventoryUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CustomSettingGUI extends GUI {

    private static final int REGENERATION_SLOT = 20;
    private static final int HUNGER_SLOT = 21;
    private static final int BUILD_SLOT = 22;
    private static final int ROUNDS_SLOT = 23;
    private static final int KNOCKBACK_SLOT = 24;
    private static final int HITDELAY_SLOT = 30;
    private static final int ENDERPEARL_SLOT = 31;
    private static final int GOLDENAPPLE_SLOT = 32;

    private final CustomLadder ladder;
    private final GUI backTo;

    public CustomSettingGUI(CustomLadder ladder, GUI backTo) {
        super(GUIType.PlayerCustom_Setting);
        this.ladder = ladder;
        this.backTo = backTo;
        this.gui.put(1, InventoryUtil.createInventory(PlayerKitManager.getInstance().getString("GUI.CUSTOM-SETTINGS.TITLE"), 6));

        this.build();
    }

    @Override
    public void build() {
        Inventory inventory = this.gui.get(1);

        inventory.setItem(45, StaticItems.BACK_TO_ITEM);

        this.update();
    }

    @Override
    public void update() {
        Inventory inventory = this.gui.get(1);

        inventory.setItem(REGENERATION_SLOT, ladder.isRegen() ? StaticItems.REGEN_ITEM.getFirst() : StaticItems.REGEN_ITEM.getSecond());
        inventory.setItem(HUNGER_SLOT, ladder.isHunger() ? StaticItems.HUNGER_ITEM.getFirst() : StaticItems.HUNGER_ITEM.getSecond());
        inventory.setItem(BUILD_SLOT, ladder.isBuild() ? StaticItems.BUILD_ITEM.getFirst() : StaticItems.BUILD_ITEM.getSecond());
        inventory.setItem(ROUNDS_SLOT, StaticItems.ROUNDS_ITEM.cloneItem().replace("%rounds%", String.valueOf(ladder.getRounds())).get());
        inventory.setItem(KNOCKBACK_SLOT, getKnockbackItem(ladder));
        inventory.setItem(HITDELAY_SLOT, StaticItems.HITDELAY_ITEM.cloneItem().replace("%hitdelay%", String.valueOf(ladder.getHitDelay())).get());
        inventory.setItem(ENDERPEARL_SLOT, StaticItems.ENDERPEARL_ITEM.cloneItem().replace("%epCooldown%", String.valueOf(ladder.getEnderPearlCooldown())).get());
        inventory.setItem(GOLDENAPPLE_SLOT, StaticItems.GAPPLE_ITEM.cloneItem().replace("%gaCooldown%", String.valueOf(ladder.getGoldenAppleCooldown())).get());

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (e.getRawSlot() < e.getInventory().getSize()) {
            e.setCancelled(true);

            switch (slot) {
                case 45:
                    backTo.open(player);
                    break;
                case REGENERATION_SLOT:
                    ladder.setRegen(!ladder.isRegen());
                    break;
                case HUNGER_SLOT:
                    ladder.setHunger(!ladder.isHunger());
                    break;
                case BUILD_SLOT:
                    ladder.setBuild(!ladder.isBuild());
                    break;
                case ROUNDS_SLOT:
                    if (e.isLeftClick() && ladder.getRounds() > 1)
                        ladder.setRounds(ladder.getRounds() - 1);
                    else if (e.isRightClick() && ladder.getRounds() < 5)
                        ladder.setRounds(ladder.getRounds() + 1);
                    break;
                case KNOCKBACK_SLOT:
                    setKnockback(ladder);
                    break;
                case HITDELAY_SLOT:
                    if (e.isLeftClick() && ladder.getHitDelay() > 1)
                        ladder.setHitDelay(ladder.getHitDelay() - 1);
                    else if (e.isRightClick() && ladder.getHitDelay() < 100)
                        ladder.setHitDelay(ladder.getHitDelay() + 1);
                    break;
                case ENDERPEARL_SLOT:
                    if (e.isLeftClick() && ladder.getEnderPearlCooldown() > 0)
                        ladder.setEnderPearlCooldown(ladder.getEnderPearlCooldown() - 1);
                    else if (e.isRightClick() && ladder.getEnderPearlCooldown() < 60)
                        ladder.setEnderPearlCooldown(ladder.getEnderPearlCooldown() + 1);
                    break;
                case GOLDENAPPLE_SLOT:
                    if (e.isLeftClick() && ladder.getGoldenAppleCooldown() > 0)
                        ladder.setGoldenAppleCooldown(ladder.getGoldenAppleCooldown() - 1);
                    else if (e.isRightClick() && ladder.getGoldenAppleCooldown() < 30)
                        ladder.setGoldenAppleCooldown(ladder.getGoldenAppleCooldown() + 1);
                    break;
                default:
                    return;
            }

            this.update();
        }
    }

    private static ItemStack getKnockbackItem(CustomLadder ladder) {
        GUIItem guiItem = StaticItems.KNOCKBACK_ITEM.cloneItem();
        List<String> extension = new ArrayList<>();
        List<String> lore = new ArrayList<>();

        for (KnockbackType kt : KnockbackType.values()) {
            String ktName = StringUtils.capitalize(kt.name().toLowerCase());

            if (ladder.getLadderKnockback().getKnockbackType().equals(kt)) extension.add(" &a» " + ktName);
            else extension.add(" &7» " + ktName);
        }

        for (String line : guiItem.getLore()) {
            if (line.contains("%knockbackTypes%"))
                lore.addAll(extension);
            else
                lore.add(line);
        }
        guiItem.setLore(lore);

        return guiItem.get();
    }

    private static void setKnockback(CustomLadder ladder) {
        switch (ladder.getLadderKnockback().getKnockbackType()) {
            case DEFAULT:
                ladder.getLadderKnockback().setKnockbackType(KnockbackType.NORMAL);
                break;
            case NORMAL:
                ladder.getLadderKnockback().setKnockbackType(KnockbackType.COMBO);
                break;
            case COMBO:
                ladder.getLadderKnockback().setKnockbackType(KnockbackType.DEFAULT);
                break;
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (GUIManager.getInstance().getOpenGUI().containsKey(player))
                return;

            backTo.open(player);
        }, 5L);
    }

}

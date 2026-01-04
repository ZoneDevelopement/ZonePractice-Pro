package dev.nandi0813.practice.manager.gui.guis.customladder.premadecustom;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CustomLadderSumGui extends GUI {

    private final Profile profile;
    private final NormalLadder ladder;

    public CustomLadderSumGui(Profile profile, NormalLadder ladder) {
        super(GUIType.CustomLadder_Summary);

        this.profile = profile;
        this.ladder = ladder;
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.KIT-EDITOR.KIT-SELECTOR.TITLE")
                        .replace("%ladder%", ladder.getDisplayName())
                        .replace("%ladderOriginal%", ladder.getName())
                , 4));

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);

            // Set kit columns
            for (int kit = 1; kit <= 4; kit++) {
                if (profile.getAllowedCustomKits() >= kit) {
                    GUIItem fillerItem = GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-SELECTOR.ICONS.FILLER-ITEM-2");

                    if (profile.getUnrankedCustomKits().get(ladder) != null && profile.getUnrankedCustomKits().get(ladder).get(kit) != null) {
                        inventory.setItem((kit - 1) * 2 + 1, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-SELECTOR.ICONS.EXISTING-KIT").replace("%kit%", String.valueOf(kit)).get());
                        inventory.setItem((kit - 1) * 2 + 1 + 9 * 2, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-SELECTOR.ICONS.EDIT-KIT").get());
                        inventory.setItem((kit - 1) * 2 + 1 + 9 * 3, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-SELECTOR.ICONS.DELETE-KIT").get());

                        inventory.setItem((kit - 1) * 2 + 1 + 9, fillerItem.get());
                    } else {
                        inventory.setItem((kit - 1) * 2 + 1, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-SELECTOR.ICONS.CREATE-KIT").get());
                        for (int i = 1; i <= 3; i++)
                            inventory.setItem((kit - 1) * 2 + 1 + 9 * i, fillerItem.get());
                    }
                } else {
                    inventory.setItem((kit - 1) * 2 + 1, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-SELECTOR.ICONS.NO-PERMISSION").get());
                    for (int i = 1; i <= 3; i++)
                        inventory.setItem((kit - 1) * 2 + 1 + 9 * i, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-SELECTOR.ICONS.FILLER-ITEM-1").get());
                }
            }

            // Set filler items between kits
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null)
                    inventory.setItem(i, GUIManager.getFILLER_ITEM());
            }

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        Inventory inventory = e.getView().getTopInventory();
        int slot = e.getRawSlot();
        ItemStack item = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() > slot && item != null) {
            if (!ladder.isEnabled() || !ladder.isEditable() || ladder.isFrozen()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.KIT-EDITOR.KIT-SELECTOR.NOT-AVAILABLE").replace("%ladder%", ladder.getDisplayName()));
                GUIManager.getInstance().searchGUI(GUIType.CustomLadder_Selector).open(player);
                return;
            }

            int kit;
            if ((slot == 1 || slot == 19 || slot == 28) && profile.getAllowedCustomKits() >= 1) kit = 1;
            else if ((slot == 3 || slot == 21 || slot == 30) && profile.getAllowedCustomKits() >= 2) kit = 2;
            else if ((slot == 5 || slot == 23 || slot == 32) && profile.getAllowedCustomKits() >= 3) kit = 3;
            else if ((slot == 7 || slot == 25 || slot == 34) && profile.getAllowedCustomKits() >= 4) kit = 4;
            else kit = -1;

            if (kit != -1) {
                if (slot == (kit - 1) * 2 + 1) {
                    this.openEditor(kit, player);
                    return;
                }

                if (profile.getUnrankedCustomKits().get(ladder).containsKey(kit)) {
                    if (slot == (kit - 1) * 2 + 1 + 9 * 2) {
                        this.openEditor(kit, player);
                    } else {
                        profile.getUnrankedCustomKits().get(ladder).remove(kit);

                        if (profile.getRankedCustomKits().containsKey(ladder)) {
                            profile.getRankedCustomKits().get(ladder).remove(kit);
                        }

                        profile.getFile().deleteCustomKit(ladder, kit);

                        this.update();
                    }
                }
            }
        } else {
            if (item != null && item.getType().equals(Material.ARROW)) {
                GUIManager.getInstance().searchGUI(GUIType.CustomLadder_Selector).open(player);
            }
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        ClassImport.getClasses().getPlayerUtil().clearInventory(player);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            if (GUIManager.getInstance().getOpenGUI().containsKey(player) && (GUIManager.getInstance().getOpenGUI().get(player) instanceof CustomLadderEditorGui))
                return;

            if (profile.getStatus().equals(ProfileStatus.MATCH) || profile.getStatus().equals(ProfileStatus.EVENT))
                return;

            InventoryManager.getInstance().setLobbyInventory(player, false);
        }, 3L);
    }

    @Override
    public void open(Player player) {
        open(player, 1);
        ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.EDITOR);

        ItemStack backToItem = GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-SELECTOR.ICONS.BACK-TO-KIT-SELECTOR").get();

        ClassImport.getClasses().getPlayerUtil().clearInventory(player);
        for (int i = 0; i < 9; i++)
            player.getInventory().setItem(i, backToItem);
    }

    private void openEditor(int kit, Player player) {
        GUI editorGui = switch (ladder.getWeightClass()) {
            case RANKED -> new CustomLadderEditorGui(profile, ladder, kit, true, this);
            case UNRANKED, UNRANKED_AND_RANKED -> new CustomLadderEditorGui(profile, ladder, kit, false, this);
        };

        editorGui.open(player);
    }

}

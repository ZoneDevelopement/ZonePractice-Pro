package dev.nandi0813.practice.manager.playerkit.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.confirmgui.ConfirmGuiType;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.playerkit.KitItems;
import dev.nandi0813.practice.manager.playerkit.PlayerKitEditing;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.playerkit.StaticItems;
import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGUI extends GUI {

    private final CustomLadder customLadder;
    private final KitItems kitItems;
    private final CustomSettingGUI customSettingGui;
    private final Map<Player, KitItem> copying = new HashMap<>();

    public MainGUI(final CustomLadder customLadder) {
        super(GUIType.PlayerCustom_MainMenu);

        this.gui.put(1, InventoryUtil.createInventory(StaticItems.MAIN_GUI_TITLE.replace("%name%", customLadder.getDisplayName()), 6));
        this.customLadder = customLadder;
        this.kitItems = new KitItems(customLadder.getKitData());
        this.customSettingGui = new CustomSettingGUI(customLadder, this);

        this.build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        inventory.setItem(0, StaticItems.MAIN_GUI_BACK);
        inventory.setItem(3, StaticItems.MAIN_GUI_SETTINGS_ICON);
        inventory.setItem(5, StaticItems.MAIN_GUI_RESET_KIT_ICON);
        inventory.setItem(7, StaticItems.MAIN_GUI_SHARE_KIT);
        inventory.setItem(8, StaticItems.MAIN_GUI_GUIDE_ICON);
        inventory.setItem(17, StaticItems.MAIN_GUI_CHANGE_NAME);

        this.update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () -> {
            Inventory inventory = gui.get(1);

            for (Map.Entry<Integer, KitItem> entry : kitItems.getSlots().entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue().getForDisplay());
            }

            this.updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        if (e.getClickedInventory() == null) {
            return;
        }

        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        int slot = e.getRawSlot();

        if (kitItems.getSlots().containsKey(slot)) {
            KitItem kitItem = kitItems.getSlots().get(slot);

            switch (e.getClick()) {
                case LEFT:
                    PlayerKitManager.getInstance().getEditing().get(player).setKitItem(kitItem);
                    GUIType guiType = switch (slot) {
                        case 10 -> GUIType.PlayerCustom_Helmet;
                        case 11 -> GUIType.PlayerCustom_Chestplate;
                        case 12 -> GUIType.PlayerCustom_Leggings;
                        case 13 -> GUIType.PlayerCustom_Boots;
                        default -> GUIType.PlayerCustom_Category;
                    };

                    GUIManager.getInstance().searchGUI(guiType).open(player);
                    break;
                case RIGHT:
                    if (!kitItem.isNull()) {
                        if (kitItem.getMaterial().getMaxStackSize() > 1) {
                            new AmountChangeGUI(kitItem, this).open(player);
                        } else if (canBeEnchanted(kitItem.get())) {
                            new EnchantGUI(kitItem, this).open(player);
                        }
                    }
                    break;
                case SHIFT_LEFT:
                    kitItem.reset();
                    this.update();
                    break;
                case SHIFT_RIGHT:
                    if (!kitItem.isNull()) {
                        copying.put(player, kitItem);
                    } else if (kitItem.isNull() && copying.containsKey(player) && !copying.get(player).isNull()) {
                        kitItem.setItemStack(copying.get(player).get());
                        this.update();
                    }
                    break;
            }
        } else {
            switch (slot) {
                case 0:
                    profile.getPlayerCustomKitSelector().update();
                    profile.getPlayerCustomKitSelector().open(player);
                    break;
                case 3:
                    customSettingGui.open(player);
                    break;
                case 5:
                    openConfirmGUI(player, ConfirmGuiType.RESET_CUSTOM_KIT, this, this);
                    break;
                case 7:
                    if (!player.hasPermission("zpp.playerkit.share")) {
                        Common.sendMMMessage(player, LanguageManager.getString("CUSTOM-PLAYER-KIT.NO-PERMISSION"));
                        return;
                    }

                    String code = null;
                    if (PlayerKitManager.getInstance().getCopy().containsValue(customLadder)) {
                        for (Map.Entry<String, CustomLadder> entry : PlayerKitManager.getInstance().getCopy().entrySet()) {
                            if (entry.getValue() == customLadder) {
                                code = entry.getKey();
                                break;
                            }
                        }
                    } else {
                        code = PlayerKitManager.getInstance().getCopyCode();
                        PlayerKitManager.getInstance().getCopy().put(code, customLadder);
                    }

                    for (String line : LanguageManager.getList("CUSTOM-PLAYER-KIT.CUSTOM-KIT-SHARE")) {
                        Common.sendMMMessage(player, line.replace("%code%", code != null ? code : "N/A"));
                    }
                    break;
                case 17:
                    new AnvilGUI.Builder()
                            .plugin(ZonePractice.getInstance())
                            .onClose(stateSnapshot ->
                                    Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
                                        this.update();
                                        this.open(player);
                                    }, 2L))
                            .onClick((anvilSlot, stateSnapshot) -> {
                                if (anvilSlot != AnvilGUI.Slot.OUTPUT) {
                                    return Collections.emptyList();
                                }

                                try {
                                    String text = stateSnapshot.getText();

                                    if (text.isEmpty()) {
                                        return List.of(AnvilGUI.ResponseAction.replaceInputText("Invalid name"));
                                    }

                                    if (text.length() < 3 || text.length() > 12) {
                                        return List.of(AnvilGUI.ResponseAction.replaceInputText("3 - 13 characters"));
                                    }

                                    customLadder.setDisplayName(text);
                                    return List.of(AnvilGUI.ResponseAction.close());
                                } catch (Exception exception) {
                                    return List.of(AnvilGUI.ResponseAction.replaceInputText("Invalid name"));
                                }
                            })
                            .text(customLadder.getDisplayName())
                            .title("Rename the kit")
                            .open(player);
                    break;
            }
        }
    }

    @Override
    public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
        if (confirmGuiType.equals(ConfirmGuiType.RESET_CUSTOM_KIT)) {
            kitItems.reset();
            this.update();
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        kitItems.save();
    }

    @Override
    public void open(Player player) {
        super.open(player);

        ClassImport.getClasses().getPlayerUtil().setActiveInventoryTitle(
                player,
                StaticItems.MAIN_GUI_TITLE.replace("%name%", customLadder.getDisplayName())
        );

        PlayerKitManager.getInstance().getEditing().remove(player);
        PlayerKitManager.getInstance().getEditing().put(player, new PlayerKitEditing(customLadder));
    }

    private static boolean canBeEnchanted(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }

        for (Enchantment enchantment : Enchantment.values()) {
            if (enchantment.canEnchantItem(itemStack)) {
                return true;
            }
        }

        return false;
    }

}

package dev.nandi0813.practice.manager.gui;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.guis.customladder.premadecustom.CustomLadderEditorGui;
import dev.nandi0813.practice.manager.gui.setup.SetupHubGui;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.sound.SoundEffect;
import dev.nandi0813.practice.manager.server.sound.SoundManager;
import dev.nandi0813.practice.manager.server.sound.SoundType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.ItemCreateUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class GUIManager implements Listener {

    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();

    private enum GUIClickSoundIntent {
        NONE,
        ADVANCE,
        BACK_OR_CLOSE
    }

    private static final List<String> BACK_OR_CLOSE_KEYWORDS = List.of("back", "close", "cancel", "return", "exit", "previous");

    private static GUIManager instance;

    public static GUIManager getInstance() {
        if (instance == null)
            instance = new GUIManager();
        return instance;
    }

    @Getter
    private final Map<GUIType, GUI> guis = new HashMap<>();
    @Getter
    private final Map<Player, GUI> openGUI = new HashMap<>();

    @Getter
    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    @Getter
    private static final ItemStack DUMMY_ITEM = ItemCreateUtil.createItem("DUMMY", Material.GLOWSTONE_DUST);

    private GUIManager() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());

        this.addGUI(new SetupHubGui());
    }

    public GUI searchGUI(GUIType type) {
        if (guis.containsKey(type))
            return guis.get(type);

        return null;
    }

    public GUI addGUI(GUI gui) {
        guis.put(gui.getType(), gui);
        return gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        GUI gui = openGUI.get(player);
        if (gui == null) return;

        if (e.getClick().equals(ClickType.DOUBLE_CLICK)) {
            return;
        }

        GUIClickSoundIntent soundIntent = resolveClickSoundIntent(e, gui, player);

        if (gui.getInConfirmationGui().containsKey(player)) {
            gui.handleConfirmGUIClick(e);
        } else {
            gui.handleClickEvent(e);
        }

        playGuiClickSound(player, soundIntent);
    }

    private GUIClickSoundIntent resolveClickSoundIntent(InventoryClickEvent e, GUI gui, Player player) {
        Inventory topInventory = e.getView().getTopInventory();
        int rawSlot = e.getRawSlot();

        if (rawSlot < 0 || rawSlot >= topInventory.getSize()) {
            return GUIClickSoundIntent.NONE;
        }

        if (gui.getInConfirmationGui().containsKey(player)) {
            return rawSlot == 4 ? GUIClickSoundIntent.ADVANCE : GUIClickSoundIntent.BACK_OR_CLOSE;
        }

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return GUIClickSoundIntent.NONE;
        }

        return isBackOrCloseItem(clickedItem) ? GUIClickSoundIntent.BACK_OR_CLOSE : GUIClickSoundIntent.ADVANCE;
    }

    private boolean isBackOrCloseItem(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;

        Component displayName = meta.displayName();
        if (displayName != null && containsBackOrCloseKeyword(PLAIN_TEXT_SERIALIZER.serialize(displayName))) {
            return true;
        }

        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }

        for (Component loreLine : lore) {
            if (containsBackOrCloseKeyword(PLAIN_TEXT_SERIALIZER.serialize(loreLine))) {
                return true;
            }
        }

        return false;
    }

    private boolean containsBackOrCloseKeyword(String text) {
        if (text == null || text.isEmpty()) return false;

        String normalized = Common.stripLegacyColor(text).toLowerCase(Locale.ROOT);
        for (String keyword : BACK_OR_CLOSE_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void playGuiClickSound(Player player, GUIClickSoundIntent intent) {
        if (intent == GUIClickSoundIntent.NONE) {
            return;
        }

        SoundType soundType = intent == GUIClickSoundIntent.BACK_OR_CLOSE
                ? SoundType.GUI_CLICK_BACK
                : SoundType.GUI_CLICK_ADVANCE;

        SoundEffect soundEffect = SoundManager.getInstance().getSound(soundType);
        if (soundEffect != null) {
            soundEffect.play(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        GUI gui = this.openGUI.get(player);
        if (gui == null) return;

        if (shouldPlayManualCloseSound(e)) {
            playGuiClickSound(player, GUIClickSoundIntent.BACK_OR_CLOSE);
        }

        gui.handleCloseEvent(e);

        // Closing the gui for the player.
        gui.close(player);
    }

    private boolean shouldPlayManualCloseSound(InventoryCloseEvent e) {
        InventoryCloseEvent.Reason reason = e.getReason();
        return reason == InventoryCloseEvent.Reason.PLAYER || reason == InventoryCloseEvent.Reason.UNKNOWN;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        GUI gui = openGUI.get(player);
        if (gui == null) return;

        gui.handleDragEvent(e);
    }

    @EventHandler
    public void onLadderEditorItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        GUI gui = openGUI.get(player);

        if (gui == null) return;

        if (profile.getStatus().equals(ProfileStatus.EDITOR) && gui instanceof CustomLadderEditorGui)
            e.getItemDrop().remove();
    }

}

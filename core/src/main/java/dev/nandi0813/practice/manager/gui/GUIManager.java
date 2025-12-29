package dev.nandi0813.practice.manager.gui;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.guis.customladder.premadecustom.CustomLadderEditorGui;
import dev.nandi0813.practice.manager.gui.setup.SetupHubGui;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import lombok.Getter;
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
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;


public class GUIManager implements Listener {

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
    private static final ItemStack DUMMY_ITEM = ClassImport.getClasses().getItemCreateUtil().createItem("DUMMY", Material.GLOWSTONE_DUST);

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

        if (gui.getInConfirmationGui().containsKey(player)) {
            gui.handleConfirmGUIClick(e);
        } else {
            gui.handleClickEvent(e);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        GUI gui = this.openGUI.get(player);
        if (gui == null) return;

        gui.handleCloseEvent(e);

        // Closing the gui for the player.
        gui.close(player);
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

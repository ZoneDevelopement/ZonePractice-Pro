package dev.nandi0813.practice.manager.gui.guis.cosmetics.shield;

import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Entry point for the shield cosmetics flow.
 * Immediately delegates to ShieldLayoutListGui.
 * Kept as a named class so CosmeticsHubGui can still reference "Shield" by this name.
 */
public class ShieldCosmeticsGui extends GUI {

    private final ShieldLayoutListGui delegate;

    public ShieldCosmeticsGui(Profile profile, GUI backToGui) {
        super(GUIType.Cosmetics_Shield);
        this.delegate = new ShieldLayoutListGui(profile, backToGui);
        // Share the delegate's inventory map so open() works correctly
        this.gui.putAll(delegate.getGui());
    }

    @Override public void build()  { delegate.build(); }
    @Override public void update() { delegate.update(); }
    @Override public void handleClickEvent(InventoryClickEvent e) { delegate.handleClickEvent(e); }

    /** Static helper kept for backwards compatibility (PlayerJoin calls this). */
    public static void applyShieldToPlayer(Player player) {
        ShieldCosmeticsUtil.applyShieldToPlayer(player);
    }
}

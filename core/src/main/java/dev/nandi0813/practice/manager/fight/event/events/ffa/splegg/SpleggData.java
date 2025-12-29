package dev.nandi0813.practice.manager.fight.event.events.ffa.splegg;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

@Getter
public class SpleggData extends EventData {

    private ItemStack eggLauncher;

    public SpleggData() {
        super(EventType.SPLEGG);
    }

    @Override
    protected void setCustomData() {

    }

    @Override
    protected void getCustomData() {
        this.eggLauncher = ConfigManager.getGuiItem("EVENT.SPLEGG.EGG-LAUNCHER-ITEM").get();
    }

    @Override
    protected void enable() throws IOException {
        if (eggLauncher == null || eggLauncher.getType().equals(Material.AIR)) {
            throw new IOException("Egg launcher item is not set. Set it in the config.");
        }
    }

}

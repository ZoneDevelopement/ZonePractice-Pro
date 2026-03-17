package dev.nandi0813.practice.manager.fight.event.events.ffa.splegg;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Getter
public class SpleggData extends EventData {

    private ItemStack eggLauncher;
    private List<String> breakableMaterials = Collections.singletonList("WOOL");

    public SpleggData() {
        super(EventType.SPLEGG);
    }

    @Override
    protected void setCustomData() {

    }

    @Override
    protected void getCustomData() {
        this.eggLauncher = ConfigManager.getGuiItem("EVENT.SPLEGG.EGG-LAUNCHER-ITEM").get();

        List<String> configuredMaterials = ConfigManager.getConfig().getStringList("EVENT.SPLEGG.BREAKABLE-MATERIALS");
        if (configuredMaterials.isEmpty()) {
            this.breakableMaterials = Collections.singletonList("WOOL");
            return;
        }

        List<String> normalizedMaterials = new ArrayList<>();
        for (String configuredMaterial : configuredMaterials) {
            if (configuredMaterial == null) {
                continue;
            }

            String normalized = configuredMaterial.trim().toUpperCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                normalizedMaterials.add(normalized);
            }
        }

        this.breakableMaterials = normalizedMaterials.isEmpty()
                ? Collections.singletonList("WOOL")
                : normalizedMaterials;
    }

    @Override
    protected void enable() throws IOException {
        if (eggLauncher == null || eggLauncher.getType().equals(Material.AIR)) {
            throw new IOException("Egg launcher item is not set. Set it in the config.");
        }
    }

}

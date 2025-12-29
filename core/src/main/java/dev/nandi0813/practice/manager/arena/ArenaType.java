package dev.nandi0813.practice.manager.arena;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

public enum ArenaType {

    BASIC(LanguageManager.getString("ARENA.ARENA-TYPES.BASIC.NAME"), Material.DIAMOND_SWORD, false, LanguageManager.getList("ARENA.ARENA-TYPES.BASIC.DESCRIPTION")),
    BUILD(LanguageManager.getString("ARENA.ARENA-TYPES.BUILD.NAME"), Material.IRON_PICKAXE, true, LanguageManager.getList("ARENA.ARENA-TYPES.BUILD.DESCRIPTION")),
    FFA(LanguageManager.getString("ARENA.ARENA-TYPES.FFA.NAME"), ClassImport.getClasses().getItemMaterialUtil().getGoldSword(), false, LanguageManager.getList("ARENA.ARENA-TYPES.FFA.DESCRIPTION"));

    private final String name;
    @Getter
    private final Material icon;
    @Getter
    private final boolean build;
    private final List<String> description;

    ArenaType(String name, Material icon, boolean build, List<String> description) {
        this.name = name;
        this.icon = icon;
        this.build = build;
        this.description = description;
    }

    public String getName() {
        return Common.mmToNormal(this.name);
    }

    public List<String> getDescription() {
        return Common.mmToNormal(this.description);
    }

}

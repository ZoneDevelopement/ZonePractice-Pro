package dev.nandi0813.practice_modern.interfaces;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldCreate implements dev.nandi0813.practice.module.interfaces.WorldCreate {

    @Override
    public World createEmptyWorld(String worldName) {
        WorldCreator wc = new WorldCreator(worldName);
        wc.type(WorldType.FLAT);
        wc.generateStructures(false);
        wc.generatorSettings("{\"layers\": [{\"block\": \"air\", \"height\": 1}, {\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}");
        return wc.createWorld();
    }

}

package dev.nandi0813.practice_1_8_8.interfaces;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldCreate implements dev.nandi0813.practice.module.interfaces.WorldCreate {

    @Override
    public World createEmptyWorld(String worldName) {
        WorldCreator wc = new WorldCreator(worldName);
        wc.type(WorldType.FLAT);
        wc.generatorSettings("2;0;1;");
        return wc.createWorld();
    }

}

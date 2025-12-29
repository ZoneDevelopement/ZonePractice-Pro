package dev.nandi0813.practice.manager.arena;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.backend.ConfigFile;

public class ArenaFile extends ConfigFile {

    public ArenaFile(DisplayArena arena) {
        super("/arenas/", arena.getName().toLowerCase());

        saveFile();
        reloadFile();
    }

    @Override
    public void setData() {
    }

    @Override
    public void getData() {
    }

}

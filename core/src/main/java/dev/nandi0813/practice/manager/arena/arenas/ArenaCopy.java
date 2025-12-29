package dev.nandi0813.practice.manager.arena.arenas;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.NormalArena;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;

@Getter
public class ArenaCopy extends NormalArena {

    protected Arena mainArena;
    private final int number;

    public ArenaCopy(String name, Arena originalArena) {
        super(name);
        this.mainArena = originalArena;
        this.number = getCopyNumber(name);
    }

    public void delete() {
        ClassImport.getClasses().getArenaCopyUtil().deleteArena(mainArena.getDisplayName(), cuboid);
        mainArena.getArenaFile().getConfig().set("copies." + name, null);
    }

    @Override
    public String getDisplayName() {
        return mainArena.getDisplayName();
    }

    private static int getCopyNumber(final String name) {
        int number;
        try {
            int lastUnderscoreIndex = name.lastIndexOf("_");
            if (lastUnderscoreIndex != -1 && lastUnderscoreIndex < name.length() - 1) {
                String numberPart = name.substring(lastUnderscoreIndex + 1);
                number = Integer.parseInt(numberPart);
                return number;
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Common.sendConsoleMMMessage("<red>Invalid copy name: " + name);
        }
        return -1;
    }

}

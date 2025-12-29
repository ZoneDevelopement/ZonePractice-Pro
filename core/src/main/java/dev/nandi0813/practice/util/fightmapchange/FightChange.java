package dev.nandi0813.practice.util.fightmapchange;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.module.interfaces.ChangedBlock;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public class FightChange {

    private final Cuboid cuboid;

    @Getter
    private Map<Location, ChangedBlock> blockChange = new HashMap<>();
    @Getter
    private Set<Entity> entityChange = new HashSet<>();

    @Getter
    private Map<Location, TempBlockChange> tempBuildPlacedBlocks = new HashMap<>();

    public FightChange(Cuboid cuboid) {
        this.cuboid = cuboid;
    }

    public void addBlockChange(ChangedBlock change) {
        Location location = change.getLocation();

        blockChange.putIfAbsent(location, change);
    }

    public void addBlockChange(ChangedBlock change, Player player, int destroyTime) {
        if (change == null) return;

        Location location = change.getLocation();

        blockChange.putIfAbsent(location, change);
        tempBuildPlacedBlocks.put(location, new TempBlockChange(this, change, player, destroyTime));
    }

    public void addEntityChange(Entity entity) {
        entityChange.add(entity);
    }

    public void rollback(int maxCheck, int maxChange) {
        for (Entity entity : entityChange) {
            if (entity.isValid()) entity.remove();
        }
        for (Entity entity : cuboid.getEntities()) {
            if (entity instanceof Player) continue;
            if (entity.isValid()) entity.remove();
        }
        entityChange = new HashSet<>();

        tempBuildPlacedBlocks.clear();
        if (blockChange.isEmpty()) return;

        if (!ZonePractice.getInstance().isEnabled()) {
            quickRollback();
            return;
        }

        Iterator<ChangedBlock> iterator = blockChange.values().iterator();
        blockChange = new HashMap<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                int changeCounter = 0;
                int checkCounter = 0;

                try {
                    while (iterator.hasNext()) {
                        if (changeCounter < maxChange && checkCounter < maxCheck) {
                            ChangedBlock changedBlock = iterator.next();

                            if (changedBlock != null) {
                                changeCounter++;

                                changedBlock.reset();

                                Block block = changedBlock.getLocation().getBlock();
                                block.removeMetadata(PLACED_IN_FIGHT, ZonePractice.getInstance());
                            }
                            checkCounter++;
                            iterator.remove();
                        } else return;
                    }
                } catch (Exception e) {
                    this.cancel();
                    Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                }
                this.cancel();
            }
        }.runTaskTimer(ZonePractice.getInstance(), 0, 1);
    }

    public void quickRollback() {
        final Iterator<ChangedBlock> iterator = blockChange.values().iterator();
        blockChange = new HashMap<>();

        while (iterator.hasNext()) {
            ChangedBlock changedBlock = iterator.next();
            changedBlock.reset();

            Block block = changedBlock.getLocation().getBlock();
            block.removeMetadata(PLACED_IN_FIGHT, ZonePractice.getInstance());

            iterator.remove();
        }
    }

}

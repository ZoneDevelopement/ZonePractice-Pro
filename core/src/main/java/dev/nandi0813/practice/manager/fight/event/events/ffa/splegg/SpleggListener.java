package dev.nandi0813.practice.manager.fight.event.events.ffa.splegg;

import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces.FFAListener;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.util.ChangedBlock;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.util.Cuboid;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import java.util.List;

public class SpleggListener extends FFAListener {

    @Override
    public void onEntityDamage(Event event, EntityDamageEvent e) {
        if (event instanceof Splegg) {
            e.setCancelled(true);
        }
    }

    @Override
    public void onEntityDamageByEntity(Event event, EntityDamageByEntityEvent e) {
        if (event instanceof Splegg) {
            if (!(e.getDamager() instanceof Egg)) {
                e.setCancelled(true);
            } else {
                e.setDamage(0);
            }
        }
    }

    @Override
    public void onProjectileLaunch(Event event, ProjectileLaunchEvent e) {

    }

    @Override
    public void onPlayerMove(Event event, PlayerMoveEvent e) {
        if (event instanceof Splegg) {
            Player player = e.getPlayer();

            Cuboid cuboid = event.getEventData().getCuboid();
            if (!cuboid.contains(e.getTo())) {
                event.killPlayer(player, false);
            } else {
                Material block = player.getLocation().getBlock().getType();
                if (block.equals(Material.WATER)) {
                    event.killPlayer(player, false);
                } else if (block.equals(Material.LAVA)) {
                    event.killPlayer(player, false);
                }
            }
        }
    }

    @Override
    public void onPlayerInteract(Event event, PlayerInteractEvent e) {
        if (event instanceof Splegg splegg) {
            Player player = e.getPlayer();

            if (!splegg.getStatus().equals(EventStatus.LIVE)) {
                return;
            }

            ItemStack item = PlayerUtil.getItemInUse(player, splegg.getEventData().getEggLauncher().getType());
            if (item != null) {
                Egg egg = player.launchProjectile(Egg.class);
                egg.customName(Component.text("SPLEGG"));
                splegg.getShotEggs().replace(player, splegg.getShotEggs().get(player) + 1);
            }
        }
    }

    @Override
    public void onPlayerEggThrow(Event event, PlayerEggThrowEvent e) {
        if (event instanceof Splegg splegg) {
            Player player = e.getPlayer();

            Egg egg = e.getEgg();
            if (!Component.text("SPLEGG").equals(egg.customName())) return;

            e.setHatching(false);
            e.setNumHatches((byte) 0);

            BlockIterator blockIterator = new BlockIterator(egg.getWorld(), egg.getLocation().toVector(), egg.getVelocity().normalize(), 0.0D, 4);
            Block hitBlock = null;

            while (blockIterator.hasNext()) {
                hitBlock = blockIterator.next();
                if (hitBlock.getType() != Material.AIR)
                    break;
            }

            if (hitBlock == null) return;
            if (!event.getEventData().getCuboid().contains(hitBlock.getLocation())) return;

            Material hitBlockType = hitBlock.getType();
            if (!isBreakableMaterial(hitBlockType, splegg.getEventData().getBreakableMaterials())) return;

            splegg.getFightChange().addBlockChange(new ChangedBlock(hitBlock));
            hitBlock.setBlockData(Material.AIR.createBlockData());
            splegg.getShotBlocks().replace(player, splegg.getShotBlocks().get(player) + 1);
        }
    }

    private boolean isBreakableMaterial(Material blockType, List<String> allowedMaterials) {
        String materialName = blockType.name();

        for (String allowedMaterial : allowedMaterials) {
            if (allowedMaterial == null || allowedMaterial.isEmpty()) {
                continue;
            }

            if (materialName.equals(allowedMaterial)) {
                return true;
            }

            if (materialName.contains("_") && materialName.endsWith("_" + allowedMaterial)) {
                return true;
            }
        }

        return false;
    }


    @Override
    public void onPlayerDropItem(Event event, PlayerDropItemEvent e) {
        if (event instanceof Splegg) {
            e.setCancelled(true);
        }
    }

}

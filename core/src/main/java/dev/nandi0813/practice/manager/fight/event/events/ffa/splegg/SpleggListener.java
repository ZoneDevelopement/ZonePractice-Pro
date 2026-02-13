package dev.nandi0813.practice.manager.fight.event.events.ffa.splegg;

import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces.FFAListener;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Cuboid;
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
                if (block.equals(Material.WATER) || block.equals(ClassImport.getClasses().getItemMaterialUtil().getWater())) {
                    event.killPlayer(player, false);
                } else if (block.equals(Material.LAVA) || block.equals(ClassImport.getClasses().getItemMaterialUtil().getLava())) {
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

            ItemStack item = ClassImport.getClasses().getPlayerUtil().getItemInUse(player, splegg.getEventData().getEggLauncher().getType());
            if (item != null) {
                Egg egg = player.launchProjectile(Egg.class);
                egg.setCustomName("SPLEGG");
                splegg.getShotEggs().replace(player, splegg.getShotEggs().get(player) + 1);
            }
        }
    }

    @Override
    public void onPlayerEggThrow(Event event, PlayerEggThrowEvent e) {
        if (event instanceof Splegg splegg) {
            Player player = e.getPlayer();

            Egg egg = e.getEgg();
            if (egg.getCustomName() == null) return;
            if (!egg.getCustomName().equals("SPLEGG")) return;

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
            String materialName = hitBlockType.name();
            // Check if block is wool - works for both 1.8.8 (WOOL) and modern versions (WHITE_WOOL, RED_WOOL, etc.)
            if (materialName.equals("WOOL") || materialName.endsWith("_WOOL")) {
                splegg.getFightChange().addBlockChange(ClassImport.createChangeBlock(hitBlock));

                hitBlock.setType(Material.AIR);
                splegg.getShotBlocks().replace(player, splegg.getShotBlocks().get(player) + 1);
            }
        }
    }

    @Override
    public void onPlayerDropItem(Event event, PlayerDropItemEvent e) {
        if (event instanceof Splegg) {
            e.setCancelled(true);
        }
    }

}

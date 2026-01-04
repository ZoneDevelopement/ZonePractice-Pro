package dev.nandi0813.practice.manager.fight.match.listener;

import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.api.Event.Match.MatchStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.util.DeleteRunnable;
import dev.nandi0813.practice.manager.fight.match.util.MatchFightPlayer;
import dev.nandi0813.practice.manager.fight.match.util.RematchRequest;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.GoldenAppleRunnable;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class LadderSettingListener implements Listener {

    @EventHandler
    public void onMatchStart(MatchStartEvent e) {
        Match match = (Match) e.getMatch();

        for (Player player : match.getPlayers())
            MatchManager.getInstance().getPlayerMatches().put(player, match);

        MatchManager.getInstance().getMatches().put(match.getId(), match);
        MatchManager.getInstance().getLiveMatches().add(match);

        // Update GUIs
        if (match instanceof Duel && ((Duel) match).isRanked())
            GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
        else
            GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();
    }


    @EventHandler
    public void onMatchEnd(MatchEndEvent e) {
        Match match = (Match) e.getMatch();

        for (Player player : match.getPlayers())
            MatchManager.getInstance().getPlayerMatches().remove(player);

        Party party = PartyManager.getInstance().getParty(match);
        if (party != null)
            party.setMatch(null);

        MatchManager.getInstance().getLiveMatches().remove(match);

        // Update GUIs
        if (match instanceof Duel && ((Duel) match).isRanked())
            GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
        else
            GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();

        SpectatorManager.getInstance().getSpectatorMenuGui().update();

        DeleteRunnable.start(match);

        // Set rematch request items
        if (ZonePractice.getInstance().isEnabled() && match.getType().equals(MatchType.DUEL) && ConfigManager.getBoolean("MATCH-SETTINGS.REMATCH.ENABLED")) {
            boolean sendRematchRequest = true;
            for (Player matchPlayer : match.getPlayers()) {
                Profile matchPlayerProfile = ProfileManager.getInstance().getProfile(matchPlayer);
                if (matchPlayerProfile.isParty()) {
                    sendRematchRequest = false;
                    break;
                }
            }

            if (sendRematchRequest) {
                RematchRequest rematchRequest = new RematchRequest(match);
                MatchManager.getInstance().getRematches().add(rematchRequest);
            }
        }
    }


    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!profile.getStatus().equals(ProfileStatus.MATCH)) return;
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (match.getLadder().isRegen()) return;
        if (e.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) return;

        e.setCancelled(true);
    }


    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!profile.getStatus().equals(ProfileStatus.MATCH)) return;
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (!match.getLadder().isHunger() || match.getCurrentStat(player).isSet()) {
            e.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;
        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block clickedBlock = e.getClickedBlock();
        if (action.equals(Action.RIGHT_CLICK_BLOCK) && clickedBlock != null) {
            if (clickedBlock.getType().equals(Material.CHEST) || clickedBlock.getType().equals(Material.TRAPPED_CHEST)) {
                match.addBlockChange(ClassImport.createChangeBlock(clickedBlock));
            }
        }
    }

    @EventHandler
    public void onGoldenHeadConsume(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        if (!item.getType().equals(Material.GOLDEN_APPLE)) return;
        if (match.getLadder().getGoldenAppleCooldown() < 1) return;
        if (item.getItemMeta().equals(ServerManager.getInstance().getGoldenHead().getItem().getItemMeta())) return;

        if (!PlayerCooldown.isActive(player, CooldownObject.GOLDEN_APPLE)) {
            GoldenAppleRunnable goldenAppleRunnable = new GoldenAppleRunnable(player, match.getLadder().getGoldenAppleCooldown());
            goldenAppleRunnable.begin();
        } else {
            e.setCancelled(true);

            Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("MATCH.COOLDOWN.GOLDEN-APPLE"), PlayerCooldown.getLeftInDouble(player, CooldownObject.GOLDEN_APPLE)));
            player.updateInventory();
        }
    }


    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        match.addEntityChange(e.getEntity());
    }


    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(e.getPlayer());
        if (match == null) return;

        if (!match.getArena().getCuboid().contains(e.getTo()))
            e.setCancelled(true);
    }


    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        match.removePlayer(player, true);
    }


    /**
     * Kit Listeners
     */
    @EventHandler
    public void onPlayerChooseKit(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
        if (!matchFightPlayer.isHasChosenKit()) {
            e.setCancelled(true);

            TeamEnum playerTeam;
            if (match instanceof Team) {
                playerTeam = ((Team) match).getTeam(player);
            } else {
                playerTeam = TeamEnum.TEAM1;
            }

            matchFightPlayer.setChosenKit(player.getInventory().getHeldItemSlot(), playerTeam);
        }
    }

    @EventHandler
    public void onPlayerChooseKit(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
        if (!matchFightPlayer.isHasChosenKit()) {
            e.setCancelled(true);
        }
    }

}

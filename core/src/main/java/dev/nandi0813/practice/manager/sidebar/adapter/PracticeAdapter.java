package dev.nandi0813.practice.manager.sidebar.adapter;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelEvent;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelFight;
import dev.nandi0813.practice.manager.fight.event.events.ffa.lms.LMS;
import dev.nandi0813.practice.manager.fight.event.events.ffa.oitc.OITC;
import dev.nandi0813.practice.manager.fight.event.events.ffa.splegg.Splegg;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.juggernaut.Juggernaut;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag.TNTTag;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.type.partyffa.PartyFFA;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partyvsparty.PartyVsParty;
import dev.nandi0813.practice.manager.fight.match.util.MatchUtil;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.type.Boxing;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.manager.queue.Queue;
import dev.nandi0813.practice.manager.queue.QueueManager;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.PAPIUtil;
import dev.nandi0813.practice.util.TPSUtil;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PracticeAdapter implements SidebarAdapter {

    @Override
    public Component getTitle(Player player) {
        return PAPIUtil.runThroughFormat(player, SidebarManager.getInstance().getConfig().getString("TITLE"));
    }

    @Override
    public List<Component> getLines(Player player) {
        YamlConfiguration config = SidebarManager.getInstance().getConfig();
        List<Component> sidebar = new ArrayList<>();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.LOBBY) ||
                profile.getStatus().equals(ProfileStatus.EDITOR) ||
                profile.getStatus().equals(ProfileStatus.STAFF_MODE) ||
                profile.getStatus().equals(ProfileStatus.CUSTOM_EDITOR)) {
            Party party = PartyManager.getInstance().getParty(player);

            if (party == null) {
                for (String line : config.getStringList("LOBBY.NORMAL")) {
                    Component component = PAPIUtil.runThroughFormat(player, line)
                            .replaceText(TextReplacementConfig.builder().match("%onlinePlayers%").replacement(String.valueOf(Bukkit.getOnlinePlayers().size())).build())
                            .replaceText(TextReplacementConfig.builder().match("%inFightPlayers%").replacement(String.valueOf(MatchManager.getInstance().getPlayerInMatchSize())).build())
                            .replaceText(TextReplacementConfig.builder().match("%inQueuePlayer%").replacement(String.valueOf(QueueManager.getInstance().getQueues().size())).build())
                            .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentFullName() : Component.empty()).build())
                            .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentShortName() : Component.empty()).build());
                    sidebar.add(component);
                }
            } else {
                for (String line : config.getStringList("LOBBY.PARTY")) {
                    Component component = PAPIUtil.runThroughFormat(player, line)
                            .replaceText(TextReplacementConfig.builder().match("%onlinePlayers%").replacement(String.valueOf(Bukkit.getOnlinePlayers().size())).build())
                            .replaceText(TextReplacementConfig.builder().match("%inFightPlayers%").replacement(String.valueOf(MatchManager.getInstance().getPlayerInMatchSize())).build())
                            .replaceText(TextReplacementConfig.builder().match("%inQueuePlayer%").replacement(String.valueOf(QueueManager.getInstance().getQueues().size())).build())
                            .replaceText(TextReplacementConfig.builder().match("%partyLeader%").replacement(party.getLeader().getName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%maxMember%").replacement(String.valueOf(party.getMaxPlayerLimit())).build())
                            .replaceText(TextReplacementConfig.builder().match("%members%").replacement(String.valueOf(party.getMembers().size())).build())
                            .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentFullName() : Component.empty()).build())
                            .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentShortName() : Component.empty()).build());
                    sidebar.add(component);
                }
            }
        } else if (profile.getStatus().equals(ProfileStatus.QUEUE)) {
            Queue queue = QueueManager.getInstance().getQueue(player);
            Event event = EventManager.getInstance().getEventByPlayer(player);

            if (queue != null) {
                for (String line : config.getStringList("LOBBY.DUEL-QUEUE")) {
                    sidebar.add(PAPIUtil.runThroughFormat(player, line)
                            .replaceText(TextReplacementConfig.builder().match("%onlinePlayers%").replacement(String.valueOf(Bukkit.getOnlinePlayers().size())).build())
                            .replaceText(TextReplacementConfig.builder().match("%inFightPlayers%").replacement(String.valueOf(MatchManager.getInstance().getPlayerInMatchSize())).build())
                            .replaceText(TextReplacementConfig.builder().match("%inQueuePlayer%").replacement(String.valueOf(QueueManager.getInstance().getQueues().size())).build())
                            .replaceText(TextReplacementConfig.builder().match("%weightClass%").replacement(queue.isRanked() ? WeightClass.RANKED.getName() : WeightClass.UNRANKED.getName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%ladderDisplayName%").replacement(queue.getLadder().getDisplayName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%elapsedTime%").replacement(queue.getFormattedDuration()).build())
                            .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentFullName() : Component.empty()).build())
                            .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentShortName() : Component.empty()).build())
                    );
                }
            } else if (event != null) {
                if (event.getQueueRunnable().getFormattedTime() != null) {
                    for (String line : config.getStringList("LOBBY.EVENT-QUEUE.STARTING")) {
                        sidebar.add(PAPIUtil.runThroughFormat(player, line)
                                .replaceText(TextReplacementConfig.builder().match("%eventName%").replacement(event.getType().getName()).build())
                                .replaceText(TextReplacementConfig.builder().match("%maxPlayer%").replacement(String.valueOf(event.getType().getMaxPlayer())).build())
                                .replaceText(TextReplacementConfig.builder().match("%player%").replacement(String.valueOf(event.getPlayers().size())).build())
                                .replaceText(TextReplacementConfig.builder().match("%timeLeft%").replacement(event.getQueueRunnable().getFormattedTime()).build())
                                .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentFullName() : Component.empty()).build())
                                .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentShortName() : Component.empty()).build())
                        );
                    }
                } else {
                    for (String line : config.getStringList("LOBBY.EVENT-QUEUE.IDLE")) {
                        sidebar.add(PAPIUtil.runThroughFormat(player, line)
                                .replaceText(TextReplacementConfig.builder().match("%eventName%").replacement(event.getType().getName()).build())
                                .replaceText(TextReplacementConfig.builder().match("%maxPlayer%").replacement(String.valueOf(event.getType().getMaxPlayer())).build())
                                .replaceText(TextReplacementConfig.builder().match("%player%").replacement(String.valueOf(event.getPlayers().size())).build())
                                .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentFullName() : Component.empty()).build())
                                .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentShortName() : Component.empty()).build())
                        );
                    }
                }
            }
        } else if (profile.getStatus().equals(ProfileStatus.MATCH)) {
            Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);

            if (match != null) {
                Ladder ladder = match.getLadder();
                Round round;
                LadderType ladderType = ladder.getType();

                String path = "MATCH.LADDER." + ladder.getName().toUpperCase() + "." + match.getType().name().toUpperCase();
                if (config.isList(path)) {
                    switch (match.getType()) {
                        case DUEL:
                            Duel duel = (Duel) match;
                            round = duel.getCurrentRound();

                            Player enemy = duel.getOppositePlayer(player);

                            for (String line : config.getStringList(path)) {
                                Component component = AdapterUtil.replaceMatchPlaceholders(player, PAPIUtil.runThroughFormat(player, line), duel);

                                switch (ladderType) {
                                    case BOXING:
                                        int playerHits = match.getCurrentStat(player) != null ? match.getCurrentStat(player).getHit() : 0;
                                        int enemyHits = match.getCurrentStat(enemy) != null ? match.getCurrentStat(enemy).getHit() : 0;
                                        int overAllHits = playerHits - enemyHits;

                                        component = component
                                                .replaceText(TextReplacementConfig.builder().matchLiteral("%overAllHits%").replacement(ZonePractice.getMiniMessage().deserialize((overAllHits < 0 ? "<red>" : "<green>")).append(Component.text(overAllHits))).build())
                                                .replaceText(TextReplacementConfig.builder().matchLiteral("%hits%").replacement(String.valueOf(playerHits)).build())
                                                .replaceText(TextReplacementConfig.builder().matchLiteral("%enemyHits%").replacement(String.valueOf(enemyHits)).build());
                                        break;
                                    case BEDWARS:
                                    case FIREBALL_FIGHT:
                                        component = component
                                                .replaceText(TextReplacementConfig.builder().matchLiteral("%playerBedStatus%").replacement(ZonePractice.getMiniMessage().deserialize(round != null && round.getBedStatus().get(duel.getTeam(player)) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED"))).build())
                                                .replaceText(TextReplacementConfig.builder().matchLiteral("%enemyBedStatus%").replacement(ZonePractice.getMiniMessage().deserialize(round != null && round.getBedStatus().get(duel.getTeam(enemy)) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED"))).build());
                                        break;
                                }

                                sidebar.add(component);
                            }
                            break;
                        case PARTY_FFA:
                            PartyFFA partyFFA = (PartyFFA) match;

                            for (String line : config.getStringList(path)) {
                                Component component = AdapterUtil.replaceMatchPlaceholders(player, PAPIUtil.runThroughFormat(player, line), partyFFA);

                                if (ladderType == LadderType.BOXING) {
                                    for (int i = 1; i <= 3; i++) {
                                        Player topPlayer = MatchUtil.getBoxingTopPlayer(partyFFA, i);
                                        Component playerName = topPlayer != null ? Component.text(topPlayer.getName()) : ZonePractice.getMiniMessage().deserialize("<red>N/A");
                                        Component playerHits = topPlayer != null ? Component.text(match.getCurrentStat(topPlayer).getHit()) : ZonePractice.getMiniMessage().deserialize("<red>N/A");

                                        component = component
                                                .replaceText(TextReplacementConfig.builder().matchLiteral("%player" + i + "boxing%").replacement(playerName).build())
                                                .replaceText(TextReplacementConfig.builder().matchLiteral("%player" + i + "boxingHits%").replacement(playerHits).build());
                                    }
                                }

                                sidebar.add(component);
                            }
                            break;
                        case PARTY_SPLIT:
                            PartySplit partySplit = (PartySplit) match;
                            round = partySplit.getCurrentRound();

                            for (String line : config.getStringList(path)) {
                                Component component = AdapterUtil.replaceMatchPlaceholders(player, PAPIUtil.runThroughFormat(player, line), partySplit);

                                component = switch (ladderType) {
                                    case BOXING -> component
                                            .replaceText(TextReplacementConfig.builder().matchLiteral("%team1boxingHits%").replacement(String.valueOf(Boxing.getTeamBoxingStrokes(match, partySplit.getTeamPlayers(TeamEnum.TEAM1)))).build())
                                            .replaceText(TextReplacementConfig.builder().matchLiteral("%team2boxingHits%").replacement(String.valueOf(Boxing.getTeamBoxingStrokes(match, partySplit.getTeamPlayers(TeamEnum.TEAM2)))).build());
                                    case BEDWARS, FIREBALL_FIGHT -> component
                                            .replaceText(TextReplacementConfig.builder().matchLiteral("%team1BedStatus%").replacement(round != null && round.getBedStatus().get(TeamEnum.TEAM1) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")).build())
                                            .replaceText(TextReplacementConfig.builder().matchLiteral("%team2BedStatus%").replacement(round != null && round.getBedStatus().get(TeamEnum.TEAM2) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")).build());
                                    default -> component;
                                };

                                sidebar.add(component);
                            }
                            break;
                        case PARTY_VS_PARTY:
                            PartyVsParty partyVsParty = (PartyVsParty) match;
                            round = partyVsParty.getCurrentRound();

                            TeamEnum team = partyVsParty.getTeam(player);
                            TeamEnum enemyTeam = TeamUtil.getOppositeTeam(team);

                            for (String line : config.getStringList(path)) {
                                Component component = AdapterUtil.replaceMatchPlaceholders(player, PAPIUtil.runThroughFormat(player, line), partyVsParty);

                                component = switch (ladderType) {
                                    case BOXING -> component
                                            .replaceText(TextReplacementConfig.builder().matchLiteral("%partyTeamBoxingHits%").replacement(String.valueOf(Boxing.getTeamBoxingStrokes(match, partyVsParty.getTeamPlayers(team)))).build())
                                            .replaceText(TextReplacementConfig.builder().matchLiteral("%enemyTeamBoxingHits%").replacement(String.valueOf(Boxing.getTeamBoxingStrokes(match, partyVsParty.getTeamPlayers(enemyTeam)))).build());
                                    case BEDWARS, FIREBALL_FIGHT -> component
                                            .replaceText(TextReplacementConfig.builder().matchLiteral("%partyTeamBedStatus%").replacement(round != null && round.getBedStatus().get(team) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")).build())
                                            .replaceText(TextReplacementConfig.builder().matchLiteral("%enemyTeamBedStatus%").replacement(round != null && round.getBedStatus().get(enemyTeam) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")).build());
                                    default -> component;
                                };

                                sidebar.add(component);
                            }
                            break;
                    }
                } else {
                    for (String line : config.getStringList("MATCH." + match.getType().name().toUpperCase())) {
                        sidebar.add(AdapterUtil.replaceMatchPlaceholders(player, PAPIUtil.runThroughFormat(player, line), match));
                    }
                }
            }
        } else if (profile.getStatus().equals(ProfileStatus.FFA)) {
            FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
            if (ffa != null) {
                if (ffa.getBuildRollback() != null) {
                    for (String line : config.getStringList("FFA.GAME.BUILD")) {
                        Component component = AdapterUtil.replaceFFAPlaceholders(player, PAPIUtil.runThroughFormat(player, line), ffa);
                        sidebar.add(component);
                    }
                } else {
                    for (String line : config.getStringList("FFA.GAME.NON-BUILD")) {
                        Component component = AdapterUtil.replaceFFAPlaceholders(player, PAPIUtil.runThroughFormat(player, line), ffa);
                        sidebar.add(component);
                    }
                }
            }
        } else if (profile.getStatus().equals(ProfileStatus.EVENT)) {
            Event event = EventManager.getInstance().getEventByPlayer(player);

            if (event != null) {
                String path = "EVENT." + event.getType().name().toUpperCase();
                switch (event.getType()) {
                    case LMS:
                        LMS lms = (LMS) event;

                        for (String line : config.getStringList(path)) {
                            Component component = PAPIUtil.runThroughFormat(player, line)
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%players%").replacement(String.valueOf(lms.getStartPlayerCount())).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%alivePlayers%").replacement(String.valueOf(lms.getPlayers().size())).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%duration%").replacement(lms.getDurationRunnable().getFormattedTime()).build());

                            sidebar.add(component);
                        }
                        break;
                    case OITC:
                        OITC oitc = (OITC) event;
                        Player highestPointPlayer = oitc.getHighestPointPlayer();

                        for (String line : config.getStringList(path)) {
                            Component component = PAPIUtil.runThroughFormat(player, line)
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%topPlayer%").replacement(highestPointPlayer.getName()).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%topScore%").replacement(String.valueOf(oitc.getPlayerPoints().get(highestPointPlayer))).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%players%").replacement(String.valueOf(oitc.getPlayerPoints().size())).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%lives%").replacement(String.valueOf(oitc.getPlayerLives().get(player))).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%alivePlayers%").replacement(String.valueOf(oitc.getPlayers().size())).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%duration%").replacement(oitc.getDurationRunnable().getFormattedTime()).build());

                            sidebar.add(component);
                        }

                        break;
                    case TNTTAG:
                        TNTTag tntTag = (TNTTag) event;

                        for (String line : config.getStringList(path)) {
                            Component component = PAPIUtil.runThroughFormat(player, line)
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%explosionTime%").replacement(tntTag.getDurationRunnable() != null ? String.valueOf(tntTag.getDurationRunnable().getSeconds()) : "0").build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%alivePlayers%").replacement(String.valueOf(tntTag.getPlayers().size())).build());

                            sidebar.add(component);
                        }
                        break;
                    case BRACKETS:
                    case SUMO:
                        DuelEvent duelEvent = (DuelEvent) event;

                        DuelFight bracketFight = duelEvent.getFight(player);
                        if (bracketFight != null) {
                            for (String line : config.getStringList(path)) {
                                Component component = PAPIUtil.runThroughFormat(player, line)
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%enemy%").replacement(bracketFight.getOtherPlayer(player).getName()).build())
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%players%").replacement(String.valueOf(duelEvent.getStartPlayerCount())).build())
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%alivePlayers%").replacement(String.valueOf(duelEvent.getPlayers().size())).build())
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%timeLeft%").replacement(duelEvent.getDurationRunnable() != null ? duelEvent.getDurationRunnable().getFormattedTime() : "0").build())
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%round%").replacement(String.valueOf(duelEvent.getRound())).build());

                                sidebar.add(component);
                            }
                        } else {
                            for (String line : config.getStringList("SPECTATE." + path)) {
                                Component component = PAPIUtil.runThroughFormat(player, line)
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%players%").replacement(String.valueOf(duelEvent.getStartPlayerCount())).build())
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%alivePlayers%").replacement(String.valueOf(duelEvent.getPlayers().size())).build())
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%timeLeft%").replacement(duelEvent.getDurationRunnable() != null ? duelEvent.getDurationRunnable().getFormattedTime() : "0").build())
                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%round%").replacement(String.valueOf(duelEvent.getRound())).build());

                                sidebar.add(component);
                            }
                        }
                        break;
                    case SPLEGG:
                        Splegg splegg = (Splegg) event;

                        for (String line : config.getStringList(path)) {
                            Component component = PAPIUtil.runThroughFormat(player, line)
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%players%").replacement(String.valueOf(splegg.getStartPlayerCount())).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%alivePlayers%").replacement(String.valueOf(splegg.getPlayers().size())).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%timeLeft%").replacement(splegg.getDurationRunnable() != null ? splegg.getDurationRunnable().getFormattedTime() : "0").build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%firedEggs%").replacement(String.valueOf(splegg.getShotEggs().get(player))).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%brokenBlocks%").replacement(String.valueOf(splegg.getShotBlocks().get(player))).build());

                            sidebar.add(component);
                        }
                        break;
                    case JUGGERNAUT:
                        Juggernaut juggernaut = (Juggernaut) event;

                        for (String line : config.getStringList(path)) {
                            Component component = PAPIUtil.runThroughFormat(player, line)
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%players%").replacement(String.valueOf(juggernaut.getStartPlayerCount())).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%alivePlayers%").replacement(String.valueOf(juggernaut.getPlayers().size() - 1)).build())
                                    .replaceText(TextReplacementConfig.builder().matchLiteral("%duration%").replacement(juggernaut.getDurationRunnable().getFormattedTime()).build());

                            sidebar.add(component);
                        }
                        break;
                }
            }
        } else if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(player);

            if (spectatable instanceof Match match) {
                LadderType ladderType = match.getLadder().getType();
                Round round;

                String path = "SPECTATE.MATCH.LADDER." + match.getLadder().getName().toUpperCase() + "." + match.getType().name().toUpperCase();
                if (config.isList(path)) {
                    switch (match.getType()) {
                        case DUEL:
                            Duel duel = (Duel) match;
                            round = duel.getCurrentRound();

                            for (String line : config.getStringList(path)) {
                                switch (ladderType) {
                                    case BOXING:
                                        Statistic player1stats = match.getCurrentStat(duel.getPlayer1());
                                        Statistic player2stats = match.getCurrentStat(duel.getPlayer2());
                                        line = line
                                                .replaceAll("%player1hits%", String.valueOf(player1stats != null ? player1stats.getHit() : 0))
                                                .replaceAll("%player2hits%", String.valueOf(player2stats != null ? player2stats.getHit() : 0));
                                        break;
                                    case BEDWARS:
                                    case FIREBALL_FIGHT:
                                        line = line
                                                .replaceAll("%player1BedStatus%", (round != null && round.getBedStatus().get(duel.getTeam(duel.getPlayer1())) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")))
                                                .replaceAll("%player2BedStatus%", (round != null && round.getBedStatus().get(duel.getTeam(duel.getPlayer2())) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")));
                                        break;
                                }

                                sidebar.add(AdapterUtil.replaceMatchSpectatePlaceholders(PAPIUtil.runThroughFormat(player, line), duel));
                            }
                            break;
                        case PARTY_FFA:
                            PartyFFA partyFFA = (PartyFFA) match;

                            for (String line : config.getStringList(path)) {
                                if (Objects.requireNonNull(ladderType) == LadderType.BOXING) {
                                    Player player1 = MatchUtil.getBoxingTopPlayer(partyFFA, 1);
                                    if (player1 != null) {
                                        line = line
                                                .replaceAll("%player1boxing%", player1.getName())
                                                .replaceAll("%player1boxingHits%", String.valueOf(match.getCurrentStat(player1).getHit()));
                                    } else {
                                        line = line
                                                .replaceAll("%player1boxing%", "<red>N/A")
                                                .replaceAll("%player1boxingHits%", "<red>N/A");
                                    }

                                    Player player2 = MatchUtil.getBoxingTopPlayer(partyFFA, 2);
                                    if (player2 != null) {
                                        line = line
                                                .replaceAll("%player2boxing%", player2.getName())
                                                .replaceAll("%player2boxingHits%", String.valueOf(match.getCurrentStat(player2).getHit()));
                                    } else {
                                        line = line
                                                .replaceAll("%player2boxing%", "<red>N/A")
                                                .replaceAll("%player2boxingHits%", "<red>N/A");
                                    }

                                    Player player3 = MatchUtil.getBoxingTopPlayer(partyFFA, 3);
                                    if (player3 != null) {
                                        line = line
                                                .replaceAll("%player3boxing%", player3.getName())
                                                .replaceAll("%player3boxingHits%", String.valueOf(match.getCurrentStat(player3).getHit()));
                                    } else {
                                        line = line
                                                .replaceAll("%player3boxing%", "<red>N/A")
                                                .replaceAll("%player3boxingHits%", "<red>N/A");
                                    }
                                }

                                sidebar.add(AdapterUtil.replaceMatchSpectatePlaceholders(PAPIUtil.runThroughFormat(player, line), partyFFA));
                            }
                            break;
                        case PARTY_SPLIT:
                            PartySplit partySplit = (PartySplit) match;
                            round = partySplit.getCurrentRound();

                            for (String line : config.getStringList(path)) {
                                switch (ladderType) {
                                    case BOXING:
                                        line = line
                                                .replaceAll("%team1boxingHits%", String.valueOf(Boxing.getTeamBoxingStrokes(match, partySplit.getTeamPlayers(TeamEnum.TEAM1))))
                                                .replaceAll("%team2boxingHits%", String.valueOf(Boxing.getTeamBoxingStrokes(match, partySplit.getTeamPlayers(TeamEnum.TEAM2))));
                                        break;
                                    case BEDWARS:
                                    case FIREBALL_FIGHT:
                                        line = line
                                                .replaceAll("%team1BedStatus%", (round.getBedStatus().get(TeamEnum.TEAM1) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")))
                                                .replaceAll("%team2BedStatus%", (round.getBedStatus().get(TeamEnum.TEAM2) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")));
                                        break;
                                }

                                sidebar.add(AdapterUtil.replaceMatchSpectatePlaceholders(PAPIUtil.runThroughFormat(player, line), partySplit));
                            }
                            break;
                        case PARTY_VS_PARTY:
                            PartyVsParty partyVsParty = (PartyVsParty) match;
                            round = partyVsParty.getCurrentRound();

                            for (String line : config.getStringList(path)) {
                                switch (ladderType) {
                                    case BOXING:
                                        line = line
                                                .replaceAll("%team1boxingHits%", String.valueOf(Boxing.getTeamBoxingStrokes(match, partyVsParty.getTeamPlayers(TeamEnum.TEAM1))))
                                                .replaceAll("%team2boxingHits%", String.valueOf(Boxing.getTeamBoxingStrokes(match, partyVsParty.getTeamPlayers(TeamEnum.TEAM2))));
                                        break;
                                    case BEDWARS:
                                    case FIREBALL_FIGHT:
                                        line = line
                                                .replaceAll("%team1BedStatus%", (round.getBedStatus().get(TeamEnum.TEAM1) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")))
                                                .replaceAll("%team2BedStatus%", (round.getBedStatus().get(TeamEnum.TEAM2) ? config.getString("MATCH.BED-STATUS.NOT-DESTROYED") : config.getString("MATCH.BED-STATUS.DESTROYED")));
                                        break;
                                }

                                sidebar.add(AdapterUtil.replaceMatchSpectatePlaceholders(PAPIUtil.runThroughFormat(player, line), partyVsParty));
                            }
                            break;
                    }
                } else {
                    for (String line : config.getStringList("SPECTATE.MATCH." + match.getType().name().toUpperCase())) {
                        sidebar.add(AdapterUtil.replaceMatchSpectatePlaceholders(PAPIUtil.runThroughFormat(player, line), match));
                    }
                }
            } else if (spectatable instanceof FFA ffa) {
                if (ffa.getBuildRollback() != null) {
                    for (String line : config.getStringList("FFA.SPECTATE.BUILD")) {
                        sidebar.add(AdapterUtil.replaceFFASpecPlaceholders(PAPIUtil.runThroughFormat(player, line), ffa));
                    }
                } else {
                    for (String line : config.getStringList("FFA.SPECTATE.NON-BUILD")) {
                        sidebar.add(AdapterUtil.replaceFFASpecPlaceholders(PAPIUtil.runThroughFormat(player, line), ffa));
                    }
                }
            } else if (spectatable instanceof Event event) {
                String path = "SPECTATE.EVENT." + event.getType().name().toUpperCase();
                switch (event.getType()) {
                    case LMS:
                        LMS lms = (LMS) event;

                        for (String line : config.getStringList(path)) {
                            line = line
                                    .replaceAll("%players%", String.valueOf(lms.getStartPlayerCount()))
                                    .replaceAll("%alivePlayers%", String.valueOf(lms.getPlayers().size()))
                                    .replaceAll("%duration%", lms.getDurationRunnable().getFormattedTime());

                            sidebar.add(PAPIUtil.runThroughFormat(player, line));
                        }
                        break;
                    case OITC:
                        OITC oitc = (OITC) event;
                        Player highestPointPlayer = oitc.getHighestPointPlayer();

                        for (String line : config.getStringList(path)) {
                            line = line
                                    .replaceAll("%topPlayer%", highestPointPlayer.getName())
                                    .replaceAll("%topScore%", String.valueOf(oitc.getPlayerPoints().get(highestPointPlayer)))
                                    .replaceAll("%players%", String.valueOf(oitc.getPlayerPoints().size()))
                                    .replaceAll("%alivePlayers%", String.valueOf(oitc.getPlayers().size()))
                                    .replaceAll("%duration%", oitc.getDurationRunnable().getFormattedTime());

                            sidebar.add(PAPIUtil.runThroughFormat(player, line));
                        }

                        break;
                    case TNTTAG:
                        TNTTag tntTag = (TNTTag) event;

                        for (String line : config.getStringList(path)) {
                            line = line
                                    .replaceAll("%explosionTime%", (tntTag.getDurationRunnable() != null ? String.valueOf(tntTag.getDurationRunnable().getSeconds()) : "0"))
                                    .replaceAll("%alivePlayers%", String.valueOf(tntTag.getPlayers().size()));

                            sidebar.add(PAPIUtil.runThroughFormat(player, line));
                        }
                        break;
                    case BRACKETS:
                    case SUMO:
                        DuelEvent duelEvent = (DuelEvent) event;

                        for (String line : config.getStringList(path)) {
                            line = line
                                    .replaceAll("%players%", String.valueOf(duelEvent.getStartPlayerCount()))
                                    .replaceAll("%alivePlayers%", String.valueOf(duelEvent.getPlayers().size()))
                                    .replaceAll("%timeLeft%", (duelEvent.getDurationRunnable() != null ? duelEvent.getDurationRunnable().getFormattedTime() : "0"))
                                    .replaceAll("%round%", String.valueOf(duelEvent.getRound()));

                            sidebar.add(PAPIUtil.runThroughFormat(player, line));
                        }
                        break;
                    case SPLEGG:
                        Splegg splegg = (Splegg) event;

                        for (String line : config.getStringList(path)) {
                            line = line
                                    .replaceAll("%players%", String.valueOf(splegg.getStartPlayerCount()))
                                    .replaceAll("%alivePlayers%", String.valueOf(splegg.getPlayers().size()))
                                    .replaceAll("%timeLeft%", (splegg.getDurationRunnable() != null ? splegg.getDurationRunnable().getFormattedTime() : "0"));

                            sidebar.add(PAPIUtil.runThroughFormat(player, line));
                        }
                        break;
                    case JUGGERNAUT:
                        Juggernaut juggernaut = (Juggernaut) event;

                        for (String line : config.getStringList(path)) {
                            line = line
                                    .replaceAll("%players%", String.valueOf(juggernaut.getStartPlayerCount()))
                                    .replaceAll("%alivePlayers%", String.valueOf(juggernaut.getPlayers().size() - 1))
                                    .replaceAll("%duration%", juggernaut.getDurationRunnable().getFormattedTime());

                            sidebar.add(PAPIUtil.runThroughFormat(player, line));
                        }
                        break;
                }
            }
        }

        Group group = profile.getGroup();
        if (group != null && group.getSidebarExtension() != null && !group.getSidebarExtension().isEmpty()) {
            sidebar.addAll(group.getSidebarExtension());
        }

        if (player.hasPermission("zpp.admin.scoreboard")) {
            for (String line : config.getStringList("ADMIN-EXTENSION")) {
                line = line
                        .replaceAll("%tps%", String.valueOf(TPSUtil.get1MinTPSRounded()))
                        .replaceAll("%arenas%", String.valueOf(ArenaManager.getInstance().getArenaList().size()))
                        .replaceAll("%enabledArenas%", String.valueOf(ArenaManager.getInstance().getEnabledArenas().size()));

                sidebar.add(PAPIUtil.runThroughFormat(player, line));
            }
        }

        return sidebar;
    }

}

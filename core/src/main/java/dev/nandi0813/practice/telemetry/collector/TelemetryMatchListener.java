package dev.nandi0813.practice.telemetry.collector;

import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.api.Event.Match.MatchStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.type.duel.DuelRound;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.statistics.LadderStats;
import dev.nandi0813.practice.telemetry.MatchTelemetry;
import dev.nandi0813.practice.telemetry.PlayerTelemetry;
import dev.nandi0813.practice.telemetry.RoundTelemetry;
import dev.nandi0813.practice.telemetry.ServerFingerprintUtil;
import dev.nandi0813.practice.telemetry.bootstrap.TelemetryBootstrap;
import dev.nandi0813.practice.telemetry.transport.ai.AiTrainingLogger;
import dev.nandi0813.practice.telemetry.transport.ai.AiTrainingMatchPayload;
import dev.nandi0813.practice.telemetry.transport.regular.TelemetryLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TelemetryMatchListener implements Listener {

    private static final int SCHEMA_VERSION = 1;
    private static final int MAX_CONCURRENT_AI_RECORDINGS = 5;
    private final Map<String, MatchStartSnapshot> snapshots = new ConcurrentHashMap<>();
    private final Map<String, AiTrainingCollector> aiCollectors = new ConcurrentHashMap<>();

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        Match match = (Match) event.getMatch();
        Map<UUID, Integer> eloBefore = new HashMap<>();

        if (match.getLadder() instanceof NormalLadder normalLadder) {
            for (Player player : match.getPlayers()) {
                Profile profile = ProfileManager.getInstance().getProfile(player);
                if (profile == null) {
                    continue;
                }

                LadderStats ladderStats = profile.getStats().getLadderStat(normalLadder);
                eloBefore.put(player.getUniqueId(), ladderStats.getElo());
            }
        }

        snapshots.put(match.getId(), new MatchStartSnapshot(System.currentTimeMillis(), eloBefore));

        if (AiTrainingCollector.isSupportedLadder(match)) {
            if (aiCollectors.size() >= MAX_CONCURRENT_AI_RECORDINGS) {
                // Bukkit.getLogger().warning("[ZonePractice] Skipping AI recording for match " + match.getId() + " (active recordings cap=" + MAX_CONCURRENT_AI_RECORDINGS + ")");
                return;
            }

            AiTrainingCollector collector = new AiTrainingCollector(match);
            aiCollectors.put(match.getId(), collector);
            collector.start();
        }
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        Match match = (Match) event.getMatch();
        MatchStartSnapshot snapshot = snapshots.remove(match.getId());
        String telemetryMatchId = toTelemetryMatchUuid(match.getId());

        long matchEndTs = System.currentTimeMillis();
        long matchStartTs = snapshot != null ? snapshot.startTs() : (matchEndTs - (match.getDuration() * 1000L));

        Object winnerObject = match.getMatchWinner();
        UUID winnerUuid = winnerObject instanceof Player winnerPlayer ? winnerPlayer.getUniqueId() : null;

        MatchTelemetry telemetry = new MatchTelemetry(
                SCHEMA_VERSION,
                telemetryMatchId,
                match.getType().name(),
                match.getLadder().getName(),
                match.getArena().getName(),
                isRanked(match),
                match.getWinsNeeded(),
                ServerFingerprintUtil.getServerId(),
                ZonePractice.getInstance().getPluginMeta().getVersion(),
                Bukkit.getBukkitVersion(),
                matchStartTs,
                matchEndTs,
                Math.max(0L, matchEndTs - matchStartTs),
                determineTerminationReason(match, winnerUuid),
                buildPlayerTelemetry(match, winnerUuid, snapshot),
                buildRoundTelemetry(match, matchStartTs),
                new ArrayList<>(),
                TelemetryLogger.getDroppedRecords(),
                System.currentTimeMillis()
        );

        if (TelemetryBootstrap.isActive()) {
            TelemetryLogger.logAsync(telemetry);
        }
        if (TelemetryBootstrap.isAiCollectionActive()) {
            AiTrainingCollector collector = aiCollectors.remove(match.getId());
            if (collector != null) {
                collector.stop();
                if (collector.hasRows()) {
                    AiTrainingMatchPayload aiPayload = collector.toPayload();
                    AiTrainingLogger.logAsync(aiPayload);
                }
            }
        } else {
            AiTrainingCollector collector = aiCollectors.remove(match.getId());
            if (collector != null) {
                collector.stop();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR
                && action != Action.LEFT_CLICK_BLOCK
                && action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        AiTrainingCollector collector = getAiCollectorByPlayer(event.getPlayer());
        if (collector == null) {
            return;
        }

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            collector.markLmb(event.getPlayer());
        } else {
            collector.markRmb(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        AiTrainingCollector collector = getAiCollectorByPlayer(player);
        if (collector != null) {
            collector.markRmb(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        AiTrainingCollector collector = getAiCollectorByPlayer(player);
        if (collector != null) {
            collector.markInventoryOpen(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        AiTrainingCollector collector = getAiCollectorByPlayer(event.getPlayer());
        if (collector != null) {
            collector.incrementBlocksPlaced(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player damager) {
            attacker = damager;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker == null) {
            return;
        }

        AiTrainingCollector attackerCollector = getAiCollectorByPlayer(attacker);
        AiTrainingCollector targetCollector = getAiCollectorByPlayer(target);
        if (attackerCollector == null || attackerCollector != targetCollector) {
            return;
        }

        attackerCollector.markLmb(attacker);
        attackerCollector.addDamageDealt(attacker, event.getFinalDamage());
        attackerCollector.addDamageTaken(target, event.getFinalDamage());
    }

    private AiTrainingCollector getAiCollectorByPlayer(Player player) {
        Match match = dev.nandi0813.practice.manager.fight.match.MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) {
            return null;
        }

        return aiCollectors.get(match.getId());
    }

    private List<PlayerTelemetry> buildPlayerTelemetry(Match match, UUID winnerUuid, MatchStartSnapshot snapshot) {
        List<PlayerTelemetry> players = new ArrayList<>();
        Map<UUID, AggregatedStat> aggregateByPlayer = aggregateStats(match);

        for (Player player : match.getPlayers()) {
            UUID playerUuid = player.getUniqueId();
            Player opponentPlayer = findOpponentPlayer(match, player);
            UUID opponentUuid = opponentPlayer != null ? opponentPlayer.getUniqueId() : null;
            String opponentUsername = resolveOpponentUsername(opponentPlayer);
            AggregatedStat stat = aggregateByPlayer.getOrDefault(playerUuid, new AggregatedStat());

            Integer eloBefore = snapshot != null ? snapshot.eloByPlayer().get(playerUuid) : null;
            Integer eloAfter = getCurrentElo(match, player);
            Integer eloDelta = (eloBefore != null && eloAfter != null) ? (eloAfter - eloBefore) : null;
            Integer ping = getPingSafe(player);

            long queueWaitMs = 0L;
            int queueSearchRangeStart = eloBefore != null ? eloBefore : 0;
            int queueSearchRangeEnd = eloAfter != null ? eloAfter : queueSearchRangeStart;

            players.add(new PlayerTelemetry(
                    playerUuid,
                    player.getName(),
                    opponentUuid,
                    opponentUsername,
                    eloBefore,
                    eloAfter,
                    eloDelta,
                    queueWaitMs,
                    queueSearchRangeStart,
                    queueSearchRangeEnd,
                    winnerUuid != null && winnerUuid.equals(playerUuid),
                    match.getWonRounds(player),
                    stat.kills,
                    stat.deaths,
                    stat.hitsLanded,
                    stat.hitsTaken,
                    stat.longestCombo,
                    stat.getAvgCps(),
                    stat.potionThrown,
                    stat.potionMissed,
                    stat.getPotionAccuracy(),
                    0.0D,
                    0.0D,
                    0,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D,
                    ping,
                    ping,
                    0.5D,
                    0.5D,
                    0,
                    0,
                    0
            ));
        }

        return players;
    }

    private List<RoundTelemetry> buildRoundTelemetry(Match match, long matchStartTs) {
        List<RoundTelemetry> rounds = new ArrayList<>();
        List<Round> orderedRounds = new ArrayList<>(match.getRounds().values());
        orderedRounds.sort(Comparator.comparingInt(Round::getRoundNumber));

        long rollingRoundStart = matchStartTs;
        for (Round round : orderedRounds) {
            long roundDurationMs = round.getDurationTime() * 1000L;
            long roundStartTs = rollingRoundStart;
            long roundEndTs = roundStartTs + roundDurationMs;
            rollingRoundStart = roundEndTs;

            UUID roundWinnerUuid = null;
            if (round instanceof DuelRound duelRound && duelRound.getRoundWinner() != null) {
                roundWinnerUuid = duelRound.getRoundWinner().getUniqueId();
            }

            List<RoundTelemetry.PlayerRoundTelemetry> roundPlayers = new ArrayList<>();
            for (Map.Entry<UUID, Statistic> entry : round.getStatistics().entrySet()) {
                Statistic stat = entry.getValue();
                roundPlayers.add(new RoundTelemetry.PlayerRoundTelemetry(
                        entry.getKey(),
                        stat.getHit(),
                        stat.getGetHit(),
                        stat.getKills(),
                        stat.getDeaths(),
                        stat.getPotionThrown(),
                        stat.getPotionMissed(),
                        stat.getPotionAccuracy(),
                        stat.getLongestCombo(),
                        stat.getAverageCPS()
                ));
            }

            rounds.add(new RoundTelemetry(
                    round.getRoundNumber(),
                    roundStartTs,
                    roundEndTs,
                    roundDurationMs,
                    roundWinnerUuid,
                    roundPlayers
            ));
        }

        return rounds;
    }

    private Map<UUID, AggregatedStat> aggregateStats(Match match) {
        Map<UUID, AggregatedStat> byPlayer = new HashMap<>();

        for (Round round : match.getRounds().values()) {
            for (Map.Entry<UUID, Statistic> entry : round.getStatistics().entrySet()) {
                AggregatedStat aggregate = byPlayer.computeIfAbsent(entry.getKey(), ignored -> new AggregatedStat());
                aggregate.add(entry.getValue());
            }
        }

        return byPlayer;
    }

    private Integer getCurrentElo(Match match, Player player) {
        if (!(match.getLadder() instanceof NormalLadder normalLadder)) {
            return null;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) {
            return null;
        }

        return profile.getStats().getLadderStat(normalLadder).getElo();
    }


    private Player findOpponentPlayer(Match match, Player player) {
        if (match instanceof Duel duel) {
            return duel.getOppositePlayer(player);
        }

        List<Player> players = match.getPlayers();
        if (players.size() != 2) {
            return null;
        }

        for (Player currentPlayer : players) {
            if (!currentPlayer.equals(player)) {
                return currentPlayer;
            }
        }

        return null;
    }

    private String resolveOpponentUsername(Player opponentPlayer) {
        if (opponentPlayer == null) {
            return "unknown";
        }

        String opponentName = opponentPlayer.getName();
        if (opponentName == null || opponentName.isBlank()) {
            return "unknown";
        }

        return opponentName;
    }


    private String toTelemetryMatchUuid(String rawMatchId) {
        if (rawMatchId == null || rawMatchId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        try {
            return UUID.fromString(rawMatchId).toString();
        } catch (IllegalArgumentException ignored) {
            return UUID.nameUUIDFromBytes(rawMatchId.getBytes(StandardCharsets.UTF_8)).toString();
        }
    }

    private boolean isRanked(Match match) {
        return match instanceof Duel duel && duel.isRanked();
    }

    private String determineTerminationReason(Match match, UUID winnerUuid) {
        if (winnerUuid == null) {
            return "draw";
        }

        if (match instanceof Duel duel && duel.getLoser() == null) {
            return "forfeit";
        }

        if (match.getLadder() instanceof NormalLadder normalLadder && match.getDuration() >= normalLadder.getMaxDuration()) {
            return "timeout";
        }

        return "completed";
    }

    private Integer getPingSafe(Player player) {
        try {
            Object value = player.getClass().getMethod("getPing").invoke(player);
            if (value instanceof Integer ping) {
                return ping;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private record MatchStartSnapshot(long startTs, Map<UUID, Integer> eloByPlayer) {
    }

    private static class AggregatedStat {
        private int hitsLanded;
        private int hitsTaken;
        private int kills;
        private int deaths;
        private int potionThrown;
        private int potionMissed;
        private int longestCombo;
        private double avgCpsSum;
        private int avgCpsSamples;

        private void add(Statistic stat) {
            hitsLanded += stat.getHit();
            hitsTaken += stat.getGetHit();
            kills += stat.getKills();
            deaths += stat.getDeaths();
            potionThrown += stat.getPotionThrown();
            potionMissed += stat.getPotionMissed();
            if (stat.getLongestCombo() > longestCombo) {
                longestCombo = stat.getLongestCombo();
            }
            if (stat.getAverageCPS() > 0) {
                avgCpsSum += stat.getAverageCPS();
                avgCpsSamples++;
            }
        }

        private int getPotionAccuracy() {
            if (potionThrown == 0) {
                return 0;
            }
            return 100 - (int) Math.ceil((potionMissed / (double) potionThrown) * 100.0D);
        }

        private double getAvgCps() {
            if (avgCpsSamples == 0) {
                return 0;
            }
            return avgCpsSum / avgCpsSamples;
        }
    }
}

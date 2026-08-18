package dev.nandi0813.practice.manager.matchhistory;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Per-player match history model. Analog to {@code Profile}, owning its own
 * {@link MatchHistoryFile} for persistence and caching the recent entries.
 * <p>
 * Entries are kept newest-first, capped at {@link #MAX_HISTORY}.
 */
@Getter
@Setter
public class MatchHistory {

    private static final int MAX_HISTORY = 5;

    private final UUID uuid;
    private final MatchHistoryFile file;
    private final List<MatchHistoryEntry> matches = new ArrayList<>();

    public MatchHistory(UUID uuid) {
        this.uuid = uuid;
        this.file = new MatchHistoryFile(this);
    }

    /**
     * Loads YAML matches into the cache. Returns the loaded matches (newest-first).
     */
    public List<MatchHistoryEntry> load() {
        file.getData();
        return matches;
    }

    /**
     * Adds a match to the front of the cache (newest-first), keeping the size within the cap.
     */
    public void add(MatchHistoryEntry match) {
        matches.addFirst(match);
        if (matches.size() > MAX_HISTORY) matches.subList(MAX_HISTORY, matches.size()).clear();
    }
}
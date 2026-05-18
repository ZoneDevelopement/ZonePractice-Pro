package dev.nandi0813.practice.manager.profile;

import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.util.CustomKit;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsData;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.nandi0813.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import dev.nandi0813.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.nandi0813.practice.manager.profile.enums.ProfilePrefixVisibility;
import dev.nandi0813.practice.manager.profile.enums.ProfileWorldTime;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.manager.profile.group.GroupManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.ItemSerializationUtil;
import dev.nandi0813.practice.util.NameFormatUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.*;

public class ProfileFile extends ConfigFile {

    private final Profile profile;

    public ProfileFile(Profile profile) {
        super("/profiles/", profile.getUuid().toString().toLowerCase());
        this.profile = profile;

        saveFile();
        reloadFile();
    }

    @Override
    public void setData() {
        setJoinTimestamps();
        setGroup();
        setPrefix();
        setSuffix();
        setNameTemplate();
        setCustomKitPerm();
        setSettings();
        saveCosmetics();
        saveCustomKits();
        saveFile();
    }

    private void setJoinTimestamps() {
        config.set("join.latest", profile.getLastJoin());
    }

    private void setGroup() {
        if (profile.getGroup() != null)
            config.set("group", profile.getGroup().getName());
        else
            config.set("group", null);
    }

    private void setPrefix() {
        if (profile.getPrefix() != null)
            config.set("prefix", dev.nandi0813.practice.ZonePractice.getMiniMessage().serialize(profile.getPrefix()));
        else
            config.set("prefix", null);
    }

    private void setSuffix() {
        if (profile.getSuffix() != null)
            config.set("suffix", dev.nandi0813.practice.ZonePractice.getMiniMessage().serialize(profile.getSuffix()));
        else
            config.set("suffix", null);
    }

    private void setNameTemplate() {
        if (profile.getNameTemplate() != null && !profile.getNameTemplate().isEmpty())
            config.set("name-template", profile.getNameTemplate());
        else
            config.set("name-template", null);
    }

    private void setCustomKitPerm() {
        int customKitPerm = profile.getCustomKitPerm();
        if (customKitPerm > 0) config.set("allowed-custom-kits", customKitPerm);
    }

    private void setSettings() {
        config.set("settings.duelrequest", profile.isDuelRequest());
        config.set("settings.sidebar", profile.isSidebar());
        config.set("settings.hideplayers", profile.isHidePlayers());
        config.set("settings.partyinvites", profile.isPartyInvites());
        config.set("settings.allowspectate", profile.isAllowSpectate());
        config.set("settings.flying", profile.isFlying());
        config.set("settings.messages", profile.isPrivateMessages());
        config.set("settings.worldtime", profile.getWorldTime().toString());
        config.set("settings.prefix-visibility", profile.getPrefixVisibility().toString());
    }

    private void saveCosmetics() {
        if (profile.getCosmeticsData() == null) return;

        config.set("cosmetics.active-tier", profile.getCosmeticsData().getActiveTier().getId());
        config.set("cosmetics.death-effect", profile.getCosmeticsData().getDeathEffect().getId());
        config.set("cosmetics.lobby-item", profile.getCosmeticsData().getLobbyItemType().name());
        config.set("cosmetics.shield.active-layout-index", profile.getCosmeticsData().getActiveShieldLayoutIndex());

        List<String> serializedShieldLayouts = profile.getCosmeticsData().getShieldLayouts().stream()
                .map(ShieldLayout::serialise)
                .toList();
        config.set("cosmetics.shield.layouts", serializedShieldLayouts);

        for (ArmorTrimTier tier : ArmorTrimTier.values()) {
            for (ArmorSlot slot : ArmorSlot.values()) {
                String basePath = "cosmetics.tiers." + tier.getId() + "." + slot.getId();

                TrimPattern pattern = profile.getCosmeticsData().getPattern(tier, slot);
                if (pattern != null) {
                    config.set(basePath + ".pattern", "minecraft:" + CosmeticsPermissionManager.getTrimId(pattern));
                } else {
                    config.set(basePath + ".pattern", null);
                }

                TrimMaterial material = profile.getCosmeticsData().getMaterial(tier, slot);
                if (material != null) {
                    config.set(basePath + ".material", "minecraft:" + CosmeticsPermissionManager.getTrimId(material));
                } else {
                    config.set(basePath + ".material", null);
                }
            }
        }
    }

    private void saveCustomKits() {
        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            String name = ladder.getName().toLowerCase();

            saveCustomKitSet(profile.getUnrankedCustomKits(), ladder, name, "unranked");
            if (ladder.isRanked())
                saveCustomKitSet(profile.getRankedCustomKits(), ladder, name, "ranked");
        }
    }

    private void saveCustomKitSet(Map<NormalLadder, Map<Integer, CustomKit>> kits, NormalLadder ladder, String name, String type) {
        if (kits.isEmpty()) return;

        Map<Integer, CustomKit> ladderKits = kits.get(ladder);
        if (ladderKits == null) return;

        for (int i = 1; i <= 4; i++) {
            CustomKit customKit = ladderKits.get(i);
            if (customKit == null) continue;

            String base = "customkit." + name + ".kit" + i + "." + type;
            config.set(base + ".inventory", ItemSerializationUtil.itemStackArrayToBase64(customKit.getInventory()));
            config.set(base + ".armor", ItemSerializationUtil.itemStackArrayToBase64(customKit.getArmor()));
            config.set(base + ".extra", ItemSerializationUtil.itemStackArrayToBase64(customKit.getExtra()));
        }
    }

    public void setDefaultData() {
        config.set("uuid", profile.getUuid().toString());
        config.set("join.first", System.currentTimeMillis());

        int customKitPerm = profile.getCustomKitPerm();
        if (customKitPerm > 0) config.set("allowed-custom-kits", customKitPerm);

        config.set("settings.duelrequest", ConfigManager.getBoolean("PLAYER.DEFAULT-SETTINGS.DUELREQUEST"));
        config.set("settings.sidebar", ConfigManager.getBoolean("PLAYER.DEFAULT-SETTINGS.SIDEBAR"));
        config.set("settings.hideplayers", ConfigManager.getBoolean("PLAYER.DEFAULT-SETTINGS.HIDEPLAYERS"));
        config.set("settings.partyinvites", ConfigManager.getBoolean("PLAYER.DEFAULT-SETTINGS.PARTYINVITES"));
        config.set("settings.allowspectate", ConfigManager.getBoolean("PLAYER.DEFAULT-SETTINGS.ALLOWSPECTATE"));
        config.set("settings.flying", ConfigManager.getBoolean("PLAYER.DEFAULT-SETTINGS.FLYING"));
        config.set("settings.messages", ConfigManager.getBoolean("PLAYER.DEFAULT-SETTINGS.PRIVATEMESSAGE"));
        String defaultWorldTime = ConfigManager.getString("PLAYER.DEFAULT-SETTINGS.WORLD-TIME");
        try {
            config.set("settings.worldtime", ProfileWorldTime.valueOf(defaultWorldTime.toUpperCase(Locale.ROOT)).toString());
        } catch (Exception ignored) {
            config.set("settings.worldtime", ProfileWorldTime.DAY.toString());
        }

        String defaultPrefixVisibility = ConfigManager.getString("PLAYER.DEFAULT-SETTINGS.PREFIX-VISIBILITY");
        try {
            config.set("settings.prefix-visibility", ProfilePrefixVisibility.valueOf(defaultPrefixVisibility.toUpperCase(Locale.ROOT)).toString());
        } catch (Exception ignored) {
            config.set("settings.prefix-visibility", ProfilePrefixVisibility.PREFIX_AND_SUFFIX.toString());
        }

        saveFile();
    }

    @Override
    public void getData() {
        loadJoinTimestamps();
        loadGroup();
        loadPrefix();
        loadSuffix();
        loadNameTemplate();
        loadCustomKitPerm();
        loadSettings();
        loadCosmetics();
        loadCustomKits();
    }

    private void loadJoinTimestamps() {
        if (config.isLong("join.first"))
            profile.setFirstJoin(config.getLong("join.first"));

        if (config.isLong("join.latest"))
            profile.setLastJoin(config.getLong("join.latest"));
    }

    private void loadGroup() {
        if (!config.isSet("group")) return;

        Group group = GroupManager.getInstance().getGroup(config.getString("group"));
        if (group != null) {
            try {
                profile.setGroup(group);
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<red>Failed to set group for " + profile.getPlayer().getName() + "! Error: " + e.getMessage());
            }
            profile.setUnrankedLeft(group.getUnrankedLimit());
            profile.setRankedLeft(group.getRankedLimit());
            profile.setEventStartLeft(group.getEventStartLimit());
        }
    }

    private void loadPrefix() {
        if (config.isString("prefix"))
            profile.setPrefix(NameFormatUtil.parseConfiguredComponent(Objects.requireNonNull(config.getString("prefix"))));
    }

    private void loadSuffix() {
        if (config.isString("suffix"))
            profile.setSuffix(NameFormatUtil.parseConfiguredComponent(Objects.requireNonNull(config.getString("suffix"))));
    }

    private void loadNameTemplate() {
        if (config.isString("name-template"))
            profile.setNameTemplate(config.getString("name-template"));
    }

    private void loadCustomKitPerm() {
        if (config.isInt("allowed-custom-kits"))
            profile.setAllowedCustomKits(config.getInt("allowed-custom-kits"));
    }

    private void loadSettings() {
        profile.setDuelRequest(config.getBoolean("settings.duelrequest"));
        profile.setSidebar(config.getBoolean("settings.sidebar"));
        profile.setHidePlayers(config.getBoolean("settings.hideplayers"));
        profile.setPartyInvites(config.getBoolean("settings.partyinvites"));
        profile.setAllowSpectate(config.getBoolean("settings.allowspectate"));
        profile.setFlying(config.getBoolean("settings.flying"));
        profile.setPrivateMessages(config.getBoolean("settings.messages"));

        String rawWorldTime = config.getString("settings.worldtime", ProfileWorldTime.DAY.toString());
        try {
            profile.setWorldTime(ProfileWorldTime.valueOf(rawWorldTime.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            profile.setWorldTime(ProfileWorldTime.DAY);
        }

        String rawPrefixVisibility = config.getString("settings.prefix-visibility", ProfilePrefixVisibility.PREFIX_AND_SUFFIX.toString());
        try {
            profile.setPrefixVisibility(ProfilePrefixVisibility.valueOf(rawPrefixVisibility.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            profile.setPrefixVisibility(ProfilePrefixVisibility.PREFIX_AND_SUFFIX);
        }
    }

    private void loadCosmetics() {
        try {
            ArmorTrimTier activeTier = ArmorTrimTier.fromId(config.getString("cosmetics.active-tier", "leather"));
            profile.getCosmeticsData().setActiveTier(activeTier);
            profile.getCosmeticsData().setDeathEffect(DeathEffect.fromId(config.getString("cosmetics.death-effect", "none")));
            profile.getCosmeticsData().setLobbyItemType(CosmeticsData.LobbyItemType.valueOf(
                    config.getString("cosmetics.lobby-item", CosmeticsData.LobbyItemType.NONE.name()).toUpperCase(Locale.ROOT)
            ));

            List<ShieldLayout> shieldLayouts = new ArrayList<>();
            for (String serializedLayout : config.getStringList("cosmetics.shield.layouts")) {
                ShieldLayout layout = ShieldLayout.deserialise(serializedLayout);
                if (layout != null) {
                    shieldLayouts.add(layout);
                }
            }
            profile.getCosmeticsData().setShieldLayouts(shieldLayouts);

            int activeShieldLayoutIndex = config.getInt("cosmetics.shield.active-layout-index", -1);
            if (activeShieldLayoutIndex < -1 || activeShieldLayoutIndex >= shieldLayouts.size()) {
                activeShieldLayoutIndex = -1;
            }
            profile.getCosmeticsData().setActiveShieldLayoutIndex(activeShieldLayoutIndex);

            boolean loadedTierData = false;
            for (ArmorTrimTier tier : ArmorTrimTier.values()) {
                for (ArmorSlot slot : ArmorSlot.values()) {
                    String basePath = "cosmetics.tiers." + tier.getId() + "." + slot.getId();

                    if (config.isString(basePath + ".pattern")) {
                        TrimPattern pattern = getTrimPatternByName(config.getString(basePath + ".pattern"));
                        if (pattern != null) {
                            profile.getCosmeticsData().setPattern(tier, slot, pattern);
                            loadedTierData = true;
                        }
                    }

                    if (config.isString(basePath + ".material")) {
                        TrimMaterial material = getTrimMaterialByName(config.getString(basePath + ".material"));
                        if (material != null) {
                            profile.getCosmeticsData().setMaterial(tier, slot, material);
                            loadedTierData = true;
                        }
                    }
                }
            }

            if (!loadedTierData) {
                for (ArmorSlot slot : ArmorSlot.values()) {
                    String legacyPath = "cosmetics." + slot.getId();
                    if (config.isString(legacyPath + ".pattern")) {
                        TrimPattern pattern = getTrimPatternByName(config.getString(legacyPath + ".pattern"));
                        if (pattern != null) {
                            profile.getCosmeticsData().setPattern(ArmorTrimTier.LEATHER, slot, pattern);
                        }
                    }
                    if (config.isString(legacyPath + ".material")) {
                        TrimMaterial material = getTrimMaterialByName(config.getString(legacyPath + ".material"));
                        if (material != null) {
                            profile.getCosmeticsData().setMaterial(ArmorTrimTier.LEATHER, slot, material);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Handle invalid cosmetics data - silently ignore for graceful handling of removed/renamed cosmetics
        }
    }

    private void loadCustomKits() {
        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            String name = ladder.getName().toLowerCase();

            profile.getUnrankedCustomKits().put(ladder, loadCustomKitSet(ladder, name, "unranked"));
            if (ladder.isRanked())
                profile.getRankedCustomKits().put(ladder, loadCustomKitSet(ladder, name, "ranked"));
        }
    }

    private Map<Integer, CustomKit> loadCustomKitSet(NormalLadder ladder, String name, String type) {
        Map<Integer, CustomKit> result = new HashMap<>();

        for (int i = 1; i <= 4; i++) {
            String base = "customkit." + name + ".kit" + i + "." + type;
            if (!config.isString(base + ".inventory")) continue;

            ItemStack[] inventory = ItemSerializationUtil.itemStackArrayFromBase64(config.getString(base + ".inventory"));
            ItemStack[] armor = config.isString(base + ".armor")
                    ? ItemSerializationUtil.itemStackArrayFromBase64(config.getString(base + ".armor"))
                    : null;
            ItemStack[] extra = ItemSerializationUtil.itemStackArrayFromBase64(config.getString(base + ".extra"));

            result.put(i, new CustomKit(null, inventory, armor, extra));
        }

        return result;
    }

    public void deleteCustomKit(Ladder ladder, int kit) {
        config.set("customkit." + ladder.getName().toLowerCase() + ".kit" + kit, null);
        saveFile();
    }

    public void deleteCustomKit(Ladder ladder) {
        config.set("customkit." + ladder.getName().toLowerCase(), null);
        saveFile();
    }

    private TrimPattern getTrimPatternByName(String name) {
        if (name == null || name.isBlank()) return null;
        String normalized = normalizeKey(name);
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).get(Key.key(normalized));
    }

    private TrimMaterial getTrimMaterialByName(String name) {
        if (name == null || name.isBlank()) return null;
        String normalized = normalizeKey(name);
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL).get(Key.key(normalized));
    }

    private String normalizeKey(String key) {
        String normalized = key.trim().toLowerCase();
        if (!normalized.contains(":")) {
            return "minecraft:" + normalized;
        }
        return normalized;
    }

}

package dev.nandi0813.practice.manager.profile;

import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.util.CustomKit;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.nandi0813.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import dev.nandi0813.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.nandi0813.practice.manager.profile.enums.ProfileWorldTime;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.manager.profile.group.GroupManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.ItemSerializationUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
        config.set("join.latest", profile.getLastJoin());

        if (profile.getGroup() != null)
            config.set("group", profile.getGroup().getName());
        else
            config.set("group", null);

        if (profile.getPrefix() != null)
            config.set("prefix", profile.getPrefix());
        else
            config.set("prefix", null);

        if (profile.getSuffix() != null)
            config.set("suffix", profile.getSuffix());
        else
            config.set("suffix", null);

        int customKitPerm = profile.getCustomKitPerm();
        if (customKitPerm > 0) config.set("allowed-custom-kits", customKitPerm);

        // Basic settings
        config.set("settings.duelrequest", profile.isDuelRequest());
        config.set("settings.sidebar", profile.isSidebar());
        config.set("settings.hideplayers", profile.isHidePlayers());
        config.set("settings.partyinvites", profile.isPartyInvites());
        config.set("settings.allowspectate", profile.isAllowSpectate());
        config.set("settings.flying", profile.isFlying());
        config.set("settings.messages", profile.isPrivateMessages());
        config.set("settings.worldtime", profile.getWorldTime().toString());

        // Cosmetics data for armor trims
        if (profile.getCosmeticsData() != null) {
            config.set("cosmetics.active-tier", profile.getCosmeticsData().getActiveTier().getId());
            config.set("cosmetics.death-effect", profile.getCosmeticsData().getDeathEffect().getId());
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

        // Ladder win/lose stats
        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            String name = ladder.getName().toLowerCase();

            for (int i = 1; i <= 4; i++) {
                if (!profile.getUnrankedCustomKits().isEmpty()) {
                    if (profile.getUnrankedCustomKits().containsKey(ladder) && profile.getUnrankedCustomKits().get(ladder).containsKey(i)) {
                        CustomKit customKit = profile.getUnrankedCustomKits().get(ladder).get(i);
                        if (customKit != null) {
                            config.set("customkit." + name + ".kit" + i + ".unranked.inventory", ItemSerializationUtil.itemStackArrayToBase64(customKit.getInventory()));
                            config.set("customkit." + name + ".kit" + i + ".unranked.extra", ItemSerializationUtil.itemStackArrayToBase64(customKit.getExtra()));
                        }
                    }
                }
            }

            if (ladder.isRanked()) {
                for (int i = 1; i <= 4; i++) {
                    if (!profile.getRankedCustomKits().isEmpty()) {
                        if (profile.getRankedCustomKits().containsKey(ladder) && profile.getRankedCustomKits().get(ladder).containsKey(i)) {
                            CustomKit customKit = profile.getRankedCustomKits().get(ladder).get(i);
                            if (customKit != null) {
                                config.set("customkit." + name + ".kit" + i + ".ranked.inventory", ItemSerializationUtil.itemStackArrayToBase64(customKit.getInventory()));
                                config.set("customkit." + name + ".kit" + i + ".ranked.extra", ItemSerializationUtil.itemStackArrayToBase64(customKit.getExtra()));
                            }
                        }
                    }
                }
            }
        }

        saveFile();
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
        config.set("settings.worldtime", ProfileWorldTime.valueOf(ConfigManager.getString("PLAYER.DEFAULT-SETTINGS.WORLD-TIME")).toString());

        saveFile();
    }

    @Override
    public void getData() {
        if (config.isLong("join.first"))
            profile.setFirstJoin(config.getLong("join.first"));

        if (config.isLong("join.latest"))
            profile.setLastJoin(config.getLong("join.latest"));

        if (config.isSet("group")) {
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

        if (config.isString("prefix"))
            profile.setPrefix(Component.text(Objects.requireNonNull(config.getString("prefix"))));

        if (config.isString("suffix"))
            profile.setSuffix(Component.text(Objects.requireNonNull(config.getString("suffix"))));

        if (config.isInt("allowed-custom-kits"))
            profile.setAllowedCustomKits(config.getInt("allowed-custom-kits"));

        profile.setDuelRequest(config.getBoolean("settings.duelrequest"));
        profile.setSidebar(config.getBoolean("settings.sidebar"));
        profile.setHidePlayers(config.getBoolean("settings.hideplayers"));
        profile.setPartyInvites(config.getBoolean("settings.partyinvites"));
        profile.setAllowSpectate(config.getBoolean("settings.allowspectate"));
        profile.setFlying(config.getBoolean("settings.flying"));
        profile.setPrivateMessages(config.getBoolean("settings.messages"));
        profile.setWorldTime(ProfileWorldTime.valueOf(config.getString("settings.worldtime")));

        // Load cosmetics data for armor trims
        try {
            ArmorTrimTier activeTier = ArmorTrimTier.fromId(config.getString("cosmetics.active-tier", "leather"));
            profile.getCosmeticsData().setActiveTier(activeTier);
            profile.getCosmeticsData().setDeathEffect(DeathEffect.fromId(config.getString("cosmetics.death-effect", "none")));

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

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            String name = ladder.getName().toLowerCase();

            // Unranked custom kit
            Map<Integer, CustomKit> unrankedInventory = new HashMap<>();
            for (int i = 1; i <= 4; i++) {
                ItemStack[] inventory;
                ItemStack[] extra;

                if (config.isString("customkit." + name.toLowerCase() + ".kit" + i + ".unranked.inventory")) {
                    inventory = ItemSerializationUtil.itemStackArrayFromBase64(config.getString("customkit." + name.toLowerCase() + ".kit" + i + ".unranked.inventory"));
                    extra = ItemSerializationUtil.itemStackArrayFromBase64(config.getString("customkit." + name.toLowerCase() + ".kit" + i + ".unranked.extra"));
                    unrankedInventory.put(i, new CustomKit(null, inventory, extra));
                }
            }
            profile.getUnrankedCustomKits().put(ladder, unrankedInventory);

            if (ladder.isRanked()) {
                // Ranked custom kit
                Map<Integer, CustomKit> rankedInventory = new HashMap<>();
                for (int i = 1; i <= 4; i++) {
                    ItemStack[] inventory;
                    ItemStack[] extra;

                    if (config.isString("customkit." + name.toLowerCase() + ".kit" + i + ".ranked.inventory")) {
                        inventory = ItemSerializationUtil.itemStackArrayFromBase64(config.getString("customkit." + name.toLowerCase() + ".kit" + i + ".ranked.inventory"));
                        extra = ItemSerializationUtil.itemStackArrayFromBase64(config.getString("customkit." + name.toLowerCase() + ".kit" + i + ".ranked.extra"));
                        rankedInventory.put(i, new CustomKit(null, inventory, extra));
                    }
                }
                profile.getRankedCustomKits().put(ladder, rankedInventory);
            }
        }
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

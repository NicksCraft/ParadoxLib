package com.ncoder.paradoxlib.enums;

import org.bukkit.Material;

public enum WoodType {
    OAK(Material.OAK_LOG, Material.OAK_WOOD),
    SPRUCE(Material.SPRUCE_LOG, Material.SPRUCE_WOOD),
    BIRCH(Material.BIRCH_LOG, Material.BIRCH_WOOD),
    JUNGLE(Material.JUNGLE_LOG, Material.JUNGLE_WOOD),
    ACACIA(Material.ACACIA_LOG, Material.ACACIA_WOOD),
    DARK_OAK(Material.DARK_OAK_LOG, Material.DARK_OAK_WOOD),
    CRIMSON(Material.CRIMSON_STEM, Material.CRIMSON_HYPHAE),
    WARPED(Material.WARPED_STEM, Material.WARPED_HYPHAE),
    MANGROVE(Material.MANGROVE_LOG, Material.MANGROVE_WOOD),
    CHERRY(Material.CHERRY_LOG, Material.CHERRY_WOOD);

    private final Material log;
    private final Material wood;
    private final Material stripped_log;
    private final Material stripped_wood;

    WoodType(final Material log, final Material wood) {
        this.log = log;
        this.wood = wood;
        this.stripped_log = Material.valueOf("STRIPPED_" + log.name());
        this.stripped_wood = Material.valueOf("STRIPPED_" + wood.name());
    }

    public static WoodType matchType(final Material material) {
        for (WoodType type : WoodType.values()) {
            if (material == type.log || material == type.wood)
                return type;
        }
        return null;
    }

    public Material getLog() {
        return log;
    }

    public Material getWood() {
        return wood;
    }

    public Material getStrippedLog() {
        return stripped_log;
    }

    public Material getStrippedWood() {
        return stripped_wood;
    }

}

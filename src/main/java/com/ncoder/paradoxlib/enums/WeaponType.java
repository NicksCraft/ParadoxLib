package com.ncoder.paradoxlib.enums;

public enum WeaponType {
    SWORD, BOW, CROSSBOW, TRIDENT, AXE;

    public static WeaponType matchType(final String type) {
        if (type.endsWith("_SWORD"))
            return SWORD;
        if (type.equals("BOW"))
            return BOW;
        if (type.equals("CROSSBOW"))
            return CROSSBOW;
        if (type.equals("TRIDENT"))
            return TRIDENT;
        if (type.endsWith("_AXE"))
            return AXE;
        return null;
    }
}

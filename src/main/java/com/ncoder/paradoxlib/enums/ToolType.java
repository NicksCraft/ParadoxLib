package com.ncoder.paradoxlib.enums;

import com.ncoder.paradoxlib.utils.CheckerUtil;

import org.bukkit.inventory.ItemStack;

public enum ToolType {
    PICKAXE, AXE, SHOVEL, HOE;

    public static ToolType matchType(final ItemStack itemStack) {
        if (CheckerUtil.isAirOrNull(itemStack))
            return null;
        String type = itemStack.getType().name();
        if (type.endsWith("_PICKAXE"))
            return PICKAXE;
        if (type.endsWith("_AXE"))
            return AXE;
        if (type.endsWith("_SHOVEL"))
            return SHOVEL;
        if (type.endsWith("_HOE"))
            return HOE;
        return null;
    }
}

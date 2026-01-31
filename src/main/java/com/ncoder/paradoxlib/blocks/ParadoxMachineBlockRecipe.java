package com.ncoder.paradoxlib.blocks;

import com.ncoder.paradoxlib.common.StackUtils;

import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.ArrayUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

final class ParadoxMachineBlockRecipe {

    private final String[] strings;
    private final int[] amounts;
    final ItemStack output;
    private Map<String, ParadoxInput> lastMatch;

    ParadoxMachineBlockRecipe(ItemStack output, ItemStack[] input) {
        this.output = output;

        Map<String, Integer> strings = new HashMap<>();
        for (ItemStack item : input) {
            if (item != null && !item.getType().isAir()) {
                String string = StackUtils.getId(item);
                if (string == null) {
                    string = item.getType().name();
                }
                strings.compute(string, (k, v) -> v == null ? item.getAmount() : v + item.getAmount());
            }
        }

        this.strings = strings.keySet().toArray(new String[0]);
        this.amounts = ArrayUtils.toPrimitive(strings.values().toArray(new Integer[0]));
    }

    boolean check(Map<String, ParadoxInput> map) {
        for (int i = 0; i < strings.length; i++) {
            ParadoxInput input = map.get(strings[i]);
            if (input == null || input.amount < amounts[i]) {
                return false;
            }
        }
        lastMatch = map;
        return true;
    }

    void consume() {
        for (int i = 0; i < strings.length; i++) {
            int consume = amounts[i];
            for (ItemStack item : lastMatch.get(strings[i]).items) {
                int amt = item.getAmount();
                if (amt >= consume) {
                    ItemUtils.consumeItem(item, consume, true);
                    break;
                } else {
                    ItemUtils.consumeItem(item, amt, true);
                    consume -= amt;
                }
            }
        }
    }

}

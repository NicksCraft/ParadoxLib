package com.ncoder.paradoxlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nonnull;

public class ConversionUtil {

    public static class LOCATION {

        public static String toString(@Nonnull Location location) {
            return String.valueOf(location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";"
                    + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch()).replace('.', ',');
        }

        public static Location toLocation(@Nonnull String string) {
            String[] split = string.replace(',', '.').split(";");
            return new Location(Bukkit.getServer().getWorld(split[0]), Double.valueOf(split[1]),
                    Double.valueOf(split[2]), Double.valueOf(split[3]), Float.valueOf(split[4]),
                    Float.valueOf(split[5]));
        }

    }

    public static class NUMERICAL {

        public static String toRoman(int number) {
            String roman = "";
            int remaining = number;
            while (remaining != 0) {
                int c = 0;
                String r = "";

                if (c == 0) {
                    if (remaining / 100 != 0) {
                        c = remaining / 100;
                        remaining -= (100 * c);
                        r = "C";
                    } else if (remaining / 50 != 0) {
                        c = remaining / 50;
                        remaining -= (50 * c);
                        r = "L";
                    } else if (remaining / 10 != 0) {
                        c = remaining / 10;
                        remaining -= (10 * c);
                        r = "X";
                    } else if (remaining > 0 && remaining < 4) {
                        c = remaining;
                        remaining = 0;
                        r = "I";
                    } else {
                        remaining = 0;
                        switch (remaining) {
                            case 4:
                                roman += "IV";
                                break;
                            case 5:
                                roman += "V";
                                break;
                            case 6:
                                roman += "VI";
                                break;
                            case 7:
                                roman += "VII";
                                break;
                            case 8:
                                roman += "VIII";
                                break;
                            case 9:
                                roman += "IX";
                                break;
                        }
                    }
                }

                if (c != 0 && !r.equals("")) {
                    for (int i = c; i > 0; i--) {
                        roman += r;
                    }
                }
            }
            return roman;
        }

    }

    public static class ENCHANTMENT {

        public static String toDisplayName(Enchantment enchantment) {
            switch (enchantment.getKey().getKey().toUpperCase()) {
                case "ARROW_DAMAGE":
                    return "Power";
                case "ARROW_FIRE":
                    return "Flame";
                case "ARROW_INFINITE":
                    return "Infinity";
                case "ARROW_KNOCKBACK":
                    return "Punch";
                case "BINDING_CURSE":
                    return "Curse of Binding";
                case "DAMAGE_ALL":
                    return "Sharpness";
                case "DAMAGE_ARTHROPODS":
                    return "Bane of Arthropods";
                case "DAMAGE_UNDEAD":
                    return "Smite";
                case "DEPTH_STRIDER":
                    return "Depth Strider";
                case "DIG_SPEED":
                    return "Efficiency";
                case "DURABILITY":
                    return "Unbreaking";
                case "FIRE_ASPECT":
                    return "Fire Aspect";
                case "FROST_WALKER":
                    return "Frost Walker";
                case "KNOCKBACK":
                    return "Knockback";
                case "LOOT_BONUS_BLOCKS":
                    return "Fortune";
                case "LOOT_BONUS_MOBS":
                    return "Looting";
                case "LUCK":
                    return "Luck of the Sea";
                case "LURE":
                    return "Lure";
                case "MENDING":
                    return "Mending";
                case "OXYGEN":
                    return "Respiration";
                case "PROTECTION_ENVIRONMENTAL":
                    return "Protection";
                case "PROTECTION_EXPLOSIONS":
                    return "Blast Protection";
                case "PROTECTION_FALL":
                    return "Feather Falling";
                case "PROTECTION_FIRE":
                    return "Fire Protection";
                case "PROTECTION_PROJECTILE":
                    return "Projectile Protection";
                case "SILK_TOUCH":
                    return "Silk Touch";
                case "SWEEPING_EDGE":
                    return "Sweeping Edge";
                case "THORNS":
                    return "Thorns";
                case "VANISHING_CURSE":
                    return "Curse of Vanishing";
                case "WATER_WORKER":
                    return "Aqua Affinity";
                default:
                    return "Unknown";
            }
        }

    }

}

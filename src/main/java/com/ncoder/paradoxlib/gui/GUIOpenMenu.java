package com.ncoder.paradoxlib.gui;

import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;

/** Used to refer to a player's "viewing session" of a given menu. */
public class GUIOpenMenu {
    /** The {@link GUIMenu} that is currently open. */
    private final GUIMenu menu;

    /** The player viewing the menu. */
    private final Player player;

    /**
     * Pairs an {@link GUIMenu} instance with a player viewing that menu.
     *
     * @param menu   The {@link GUIMenu} that is open.
     * @param player The player viewing the menu.
     */
    public GUIOpenMenu(@Nonnull GUIMenu menu, @Nonnull Player player) {
        this.menu = Objects.requireNonNull(menu);
        this.player = Objects.requireNonNull(player);
    }

    /**
     * Get the open {@link GUIMenu} instance.
     *
     * @return The menu that is open.
     */
    @Nonnull
    public GUIMenu getMenu() {
        return this.menu;
    }

    /**
     * Get the player viewing the {@link GUIMenu}.
     *
     * @return The player viewing the menu.
     */
    @Nonnull
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GUIOpenMenu))
            return false;
        GUIOpenMenu that = (GUIOpenMenu) o;
        return Objects.equals(menu, that.menu) && Objects.equals(getPlayer(), that.getPlayer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(menu, getPlayer());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GUIOpenMenu.class.getSimpleName() + "[", "]")
                .add("menu=" + menu)
                .add(String.format("player=%s (%s)", player.getUniqueId().toString(), player.getDisplayName()))
                .toString();
    }
}

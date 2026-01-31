package com.ncoder.paradoxlib.gui.button;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GUIButton {

    /** The on-click handler for this button. */
    @Nullable
    private GUIButtonListener listener;

    /** The Bukkit {@link ItemStack} that will be used as the button's icon. */
    @Nonnull
    private ItemStack icon;

    /**
     * Creates an GUIButton with the specified {@link ItemStack} as it's 'icon' in
     * the inventory.
     *
     * @param icon The desired 'icon' for the GUIButton.
     */
    public GUIButton(@Nonnull ItemStack icon) {
        this.icon = validateIcon(icon);
    }

    /**
     * Sets the {@link GUIButtonListener} to be called when the button is clicked.
     *
     * @param listener The listener to be called when the button is clicked.
     */
    public void setListener(@Nullable GUIButtonListener listener) {
        this.listener = listener;
    }

    /**
     * A chainable alias of {@link #setListener(GUIButtonListener)}.
     *
     * @param listener The listener to be called when the button is clicked.
     * @return The {@link GUIButton} the listener was applied to.
     */
    public GUIButton withListener(@Nullable GUIButtonListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Returns the {@link GUIButtonListener} that is to be executed when the button
     * is clicked.
     *
     * @return The listener to be called when the button is clicked.
     */
    @Nullable
    public GUIButtonListener getListener() {
        return listener;
    }

    /**
     * Returns the {@link ItemStack} that will be used as the GUIButton's icon in
     * the GUIMenu (GUI).
     *
     * @return The icon ({@link ItemStack}) that will be used to represent the
     *         button.
     */
    @Nonnull
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Changes the GUIButton's icon.
     *
     * @param icon The icon ({@link ItemStack}) that will be used to represent the
     *             button.
     */
    public void setIcon(@Nonnull ItemStack icon) {
        this.icon = validateIcon(icon);
    }

    /**
     * Ensure that the {@link ItemStack} will be a suitable icon.
     *
     * @param icon to check.
     * @return the icon, if it is suitable.
     * @throws IllegalArgumentException if the icon is not suitable.
     * @throws NullPointerException     if the icon is null.
     */
    @Nonnull
    private ItemStack validateIcon(@Nonnull ItemStack icon) {
        if (icon.getType() == Material.AIR) {
            throw new IllegalArgumentException("Cannot use AIR as icon.");
        }

        return Objects.requireNonNull(icon, "Don't use a null icon - remove the button instead.");
    }
}

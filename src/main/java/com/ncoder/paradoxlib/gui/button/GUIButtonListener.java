package com.ncoder.paradoxlib.gui.button;

import javax.annotation.Nonnull;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface GUIButtonListener {
    /**
     * The event handler that should be executed when an GUIButton is clicked.
     * Implement this with a lambda when you create an GUIButton.
     *
     * @param event The Bukkit/Spigot API {@link InventoryClickEvent}.
     */
    void onClick(@Nonnull InventoryClickEvent event);
}

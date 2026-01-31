package com.ncoder.paradoxlib.gui;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import com.ncoder.paradoxlib.gui.button.GUIButton;
import com.ncoder.paradoxlib.gui.button.GUIButtonListener;
import com.ncoder.paradoxlib.gui.toolbar.GUIToolbarBuilder;
import com.ncoder.paradoxlib.gui.toolbar.GUIToolbarButtonType;

/**
 * The {@link GUIMenuListenerBase} provides reusable core logic for
 * version-specific inventory listeners.
 *
 * <p>
 * You must register this class as an event listener in your plugin's
 * {@code onEnable} method by initializing there (which will register a listener
 * automatically).
 */
public class GUIMenuListenerBase {
    /** The JavaPlugin instance that this listener is operating for. */
    protected final JavaPlugin plugin;

    /**
     * Initialize an GUIMenuListenerBase for the specified {@link JavaPlugin}
     * instance.
     *
     * @param plugin that this listener is registered for.
     */
    public GUIMenuListenerBase(@Nonnull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns true if the specified inventory exists and is an GUIMenu, as that
     * implies the inventory event should be
     * handled by this {@link GUIMenuListenerBase} class.
     *
     * <p>
     * Alternatively, if the inventory is null, or the inventory is not associated
     * with the {@link GUIMenu} class
     * (i.e., <code>inventory.getHolder</code> is null), this returns false
     * signalling that the event should not be
     * processed.
     *
     * @param inventory to check.
     * @return True if inventory event should be handled by
     *         {@link GUIMenuListenerBase}, false if not.
     */
    protected boolean isGUIMenu(@Nullable Inventory inventory) {
        return inventory != null && inventory.getHolder() != null && inventory.getHolder() instanceof GUIMenu;
    }

    /**
     * In addition to the tests done by {@link #isGUIMenu(Inventory)}, this method
     * checks whether the instance of
     * {@link SpiGUI} that this listener is listening on behalf of, holds a
     * different plugin to the plugin that the
     * inventory is for.
     *
     * <p>
     * If the {@code inventory} is not an {@link GUIMenu}, or it is held by a
     * different plugin, the event should be
     * ignored by this listener instance.
     *
     * @param inventory to check.
     * @return False if the inventory event is for this plugin, true if not.
     */
    protected boolean shouldIgnoreGUI(@Nullable Inventory inventory) {
        return !isGUIMenu(inventory)
                || !Objects.equals(
                        ((GUIMenu) Objects.requireNonNull(inventory).getHolder()), plugin);
    }

    /**
     * Handles the main click event for an {@link GUIMenu}.
     *
     * <p>
     * This is a protected method, intended to be delegated to by subclasses of this
     * class and {@link Listener}, so
     * as to re-use common logic across versions whilst providing an avenue for
     * version-specific overrides.
     *
     * <p>
     * The respective inventory is first checked to ensure that it is a JavaPlugin
     * {@link GUIMenu} and, if it is, whether
     * a pagination button was clicked, finally (if not a pagination button) the
     * event is delegated to the inventory the
     * click occurred in.
     *
     * @param event to handle.
     */
    protected void handleClick(@Nonnull InventoryClickEvent event) {
        // Check if the inventory click event is one we should even care about (i.e.,
        // that the inventory is actually a
        // GUIMenu owned by the current plugin). Then, get the GUIMenu instance that
        // backs the inventory.
        final Inventory inventory = event.getClickedInventory();
        if (shouldIgnoreGUI(inventory))
            return;
        GUIMenu menu = (GUIMenu) inventory.getHolder();

        // Snapshot information like the page as soon as possible to ensure it is
        // correct by the time the event is
        // handled.
        final int page = menu.getCurrentPage();
        final int pageSize = menu.getPageSize();

        // If the action is explicitly blocked, deny the event.
        if (menu.getBlockedMenuActions().stream().anyMatch(action -> action == event.getAction())) {
            event.setResult(Event.Result.DENY);
            return;
        }

        // If the click type is not permitted, instantly deny the event and
        // do nothing else.
        if (menu.getPermittedMenuClickTypes().stream().noneMatch(type -> type == event.getClick())) {
            event.setResult(Event.Result.DENY);
            return;
        }

        // If, by default, interactions should be blocked, do that now.
        final boolean blockByDefault = menu.areDefaultInteractionsBlocked();
        if (blockByDefault)
            event.setResult(Event.Result.DENY);

        // Set up an GUIButtonListener Consumer that can be used to invoke a listener
        // where we want to.
        final Consumer<GUIButtonListener> invokeListener = listener -> listener.onClick(event);

        // Handle pagination actions if the slot is on the pagination row.
        if (event.getSlot() > pageSize) {
            // Deny by default in the toolbar row.
            event.setResult(Event.Result.DENY);

            // Compute the toolbar offset (i.e., first icon in the toolbar row is 0).
            final int offset = event.getSlot() - pageSize;
            final GUIToolbarBuilder paginationBuilder = menu.getToolbarBuilder();

            // Build the button by deferring to the builder logic in the plugin.
            final GUIToolbarButtonType paginationButtonType = GUIToolbarButtonType.getDefaultForSlot(offset);
            final GUIButton paginationButton = paginationBuilder.buildToolbarButton(offset, menu.getCurrentPage(),
                    paginationButtonType, menu);

            // Attempt to invoke the listener for the button (if it exists), then exit
            // early.
            Optional.ofNullable(paginationButton).map(GUIButton::getListener).ifPresent(invokeListener);
            return;
        }

        // If the slot is 'stickied', get the button from the first page.
        if (menu.isStickiedSlot(event.getSlot())) {
            final GUIButton button = menu.getButton(0, event.getSlot());
            Optional.ofNullable(button).map(GUIButton::getListener).ifPresent(invokeListener);
        }

        // Finally, handle the button normally.
        Optional.ofNullable(menu.getButton(page, event.getSlot()))
                .map(GUIButton::getListener)
                .ifPresent(invokeListener);
    }

    /**
     * Blocks events that occur in adjacent inventories to an GUIMenu when those
     * events would affect the GUIMenu. (For
     * example, double-clicking on an item in the player's inventory that is the
     * same as an item in the GUIMenu).
     *
     * @param event to handle.
     */
    protected void handleAdjacentClick(@Nonnull InventoryClickEvent event) {
        // If the clicked inventory is not adjacent to a GUIMenu, ignore the click
        // event.
        if (shouldIgnoreGUI(event.getView().getTopInventory()))
            return;

        // If the clicked inventory is the GUIMenu (the top inventory),
        // ignore the click event (it will be handled by handleClick).
        if (event.getClickedInventory() == event.getView().getTopInventory())
            return;

        // Otherwise, the clicked menu was the bottom inventory. Block the action in it
        // if it is one of the actions
        // blocked by the top (GUIMenu).
        final GUIMenu menu = (GUIMenu) event.getView().getTopInventory().getHolder();
        if (menu != null && menu.getBlockedMenuActions().stream().anyMatch(action -> action == event.getAction())) {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * Blocks drag events in an {@link GUIMenu} and between an {@link GUIMenu} and
     * an
     * adjacent inventory.
     *
     * @param event to handle.
     */
    protected void handleDrag(@Nonnull InventoryDragEvent event) {
        if (shouldIgnoreGUI(event.getInventory()))
            return;
        final GUIMenu menu = (GUIMenu) event.getInventory().getHolder();

        // Cancel the drag event if any of the affected slots are in the
        // GUIMenu (the top inventory).
        if (slotsIncludeTopInventory(event.getView(), event.getRawSlots())) {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * Overrides the close event for an GUIMenu, ensuring the
     * {@link GUIMenu#getOnClose()} handler is invoked when the
     * inventory is closed.
     *
     * @param event to handle.
     */
    protected void handleClose(@Nonnull InventoryCloseEvent event) {
        if (shouldIgnoreGUI(event.getInventory()))
            return;
        final GUIMenu menu = (GUIMenu) event.getInventory().getHolder();

        // Invoke the inventory's onClose if there is one.
        Optional.ofNullable(menu.getOnClose()).ifPresent(onClose -> onClose.accept(event));
    }

    /**
     * Handles the main click event for an {@link GUIMenu}.
     *
     * @param event to handle.
     * @see #handleClick(InventoryClickEvent)
     */
    @EventHandler
    public void onInventoryClick(@Nonnull InventoryClickEvent event) {
        this.handleClick(event);
    }

    /**
     * Blocks events that occur in adjacent inventories to an GUIMenu when those
     * events would affect the GUIMenu. (For
     * example, double-clicking on an item in the player's inventory that is the
     * same as an item in the GUIMenu).
     *
     * <p>
     * It is recommended that the event listener that invokes this method be defined
     * with
     * {@link org.bukkit.event.EventPriority#LOWEST}, meaning that the event handler
     * will be invoked first (allowing
     * subsequent event handlers to override it).
     *
     * @param event to handle.
     * @see #handleAdjacentClick(InventoryClickEvent)
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAdjacentInventoryClick(@Nonnull InventoryClickEvent event) {
        this.handleAdjacentClick(event);
    }

    /**
     * Blocks drag events in an {@link GUIMenu} and between an {@link GUIMenu} and
     * an
     * adjacent inventory.
     *
     * <p>
     * It is recommended that the event listener that invokes this method be defined
     * with
     * {@link org.bukkit.event.EventPriority#LOWEST}, meaning that the event handler
     * will be invoked first (allowing
     * subsequent event handlers to override it).
     *
     * @param event to handle.
     * @see #handleDrag(InventoryDragEvent)
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(@Nonnull InventoryDragEvent event) {
        this.handleDrag(event);
    }

    /**
     * Overrides the close event for an GUIMenu, ensuring the
     * {@link GUIMenu#getOnClose()} handler is invoked when the
     * inventory is closed.
     *
     * @param event to handle.
     * @see #handleClose(InventoryCloseEvent)
     */
    @EventHandler
    public void onInventoryClose(@Nonnull InventoryCloseEvent event) {
        this.handleClose(event);
    }

    /**
     * Checks whether the specified set of slots includes any slots in the top
     * inventory of the specified
     * {@link InventoryView}.
     *
     * @param view  The relevant {@link InventoryView}.
     * @param slots The set of slots to check.
     * @return True if the set of slots includes any slots in the top inventory,
     *         otherwise false.
     */
    private boolean slotsIncludeTopInventory(@Nonnull InventoryView view, @Nonnull Set<Integer> slots) {
        return slots.stream().anyMatch(slot -> {
            // If the slot is bigger than the menu's page size,
            // it's a pagination button, so we'll ignore it.
            if (slot >= view.getTopInventory().getSize())
                return false;
            // Otherwise, we'll check if the slot's converted value matches
            // its raw value. If it matches, it means the slot is in the
            // menu, so we'll return true.
            return slot == view.convertSlot(slot);
        });
    }
}

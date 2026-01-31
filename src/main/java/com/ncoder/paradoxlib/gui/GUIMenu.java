package com.ncoder.paradoxlib.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import com.ncoder.paradoxlib.gui.button.GUIButton;
import com.ncoder.paradoxlib.gui.toolbar.GUIToolbarBuilder;
import com.ncoder.paradoxlib.gui.toolbar.GUIToolbarButtonType;

/**
 * GUIMenu is used to implement the library's GUIs.
 *
 * <p>
 * This is a Minecraft 'inventory' that contains items which can have
 * programmable actions performed when they are
 * clicked. Additionally, it automatically adds 'pagination' items if the menu
 * overflows.
 *
 * <p>
 * You do not instantiate this class when you need it - as you would have done
 * with the older version of the library
 * - rather you make a call to {@link #create(String, int)} or
 * {@link #create(String, int, String)} from
 * your plugin's instance.
 *
 * <p>
 * This creates an inventory that is already associated with your plugin.
 */
public class GUIMenu implements InventoryHolder {

    /** The JavaPlugin instance that created this inventory. */
    @Nonnull
    private final JavaPlugin plugin;

    // Inventory creation parameters

    /** The title of the inventory. */
    private String title;

    /** A tag that may be used to identify the type of inventory. */
    private String id;

    /** The number of rows to display per page. */
    private int rowsPerPage;

    // Items and slots.

    /** The map of items in the inventory. */
    private final Map<Integer, GUIButton> buttons;

    /** The set of sticky slots (that should remain when the page is changed). */
    private final HashSet<Integer> stickySlots;

    /** The toolbar builder used to render this GUI's toolbar. */
    private GUIToolbarBuilder toolbarBuilder;

    /**
     * Whether the pagination functionality should be enabled. (True adds pagination
     * buttons when they're needed, false
     * does not).
     */
    private boolean enableAutomaticPagination;

    // Current state

    /** The currently selected page of the inventory. */
    private int currentPage;

    // Interaction management

    /**
     * Whether the "default" behaviors and interactions should be permitted or
     * blocked. (True prevents default behaviors
     * such as moving items in the inventory, false allows them).
     */
    private boolean blockDefaultInteractions;

    /**
     * Any actions in this list will be blocked immediately without further
     * processing if they occur in a menu.
     */
    private HashSet<InventoryAction> blockedMenuActions = new HashSet<>(Arrays.asList(DEFAULT_BLOCKED_MENU_ACTIONS));

    /**
     * Any actions in this list will be blocked if they occur in the adjacent
     * inventory to an GUIMenu.
     */
    private HashSet<InventoryAction> blockedAdjacentActions = new HashSet<>(
            Arrays.asList(DEFAULT_BLOCKED_ADJACENT_ACTIONS));

    /**
     * Any click types not in this array will be immediately prevented in this menu
     * without further processing (i.e.,
     * the button's listener will not be called).
     */
    private HashSet<ClickType> permittedMenuClickTypes;

    // Event handlers

    /** The action to be performed on close. */
    private Consumer<InventoryCloseEvent> onClose;

    /** The action to be performed on page change. */
    private Consumer<GUIMenu> onPageChange;

    // -- DEFAULT PERMITTED / BLOCKED ACTIONS -- //

    /**
     * The default set of actions that are permitted if they occur in an GUIMenu.
     */
    private static final ClickType[] DEFAULT_PERMITTED_MENU_CLICK_TYPES = new ClickType[] { ClickType.LEFT,
            ClickType.RIGHT };

    /** The default set of actions that are blocked if they occur in an GUIMenu. */
    private static final InventoryAction[] DEFAULT_BLOCKED_MENU_ACTIONS = new InventoryAction[] {
            InventoryAction.MOVE_TO_OTHER_INVENTORY, InventoryAction.COLLECT_TO_CURSOR };

    /**
     * The default set of actions that are blocked if they occur in the adjacent
     * inventory to an GUIMenu.
     */
    private static final InventoryAction[] DEFAULT_BLOCKED_ADJACENT_ACTIONS = new InventoryAction[] {
            InventoryAction.MOVE_TO_OTHER_INVENTORY, InventoryAction.COLLECT_TO_CURSOR };

    /**
     * <b>For internal use only</b>: you should probably use
     * {@link #create(String, int)} or
     * {@link #create(String, int, String)}!
     *
     * <p>
     * Used by the library internally to construct an GUIMenu. This method is not
     * considered part of the stable public
     *
     * <p>
     * The title parameter is color code translated.
     *
     * @param plugin      The Plugin instance associated with this menu.
     * @param title       The title of the menu.
     * @param rowsPerPage The number of rows per page in the menu.
     * @param id          The id associated with this menu.
     * @param clickTypes  The set of permitted click types.
     */
    public GUIMenu(@Nonnull JavaPlugin plugin, String title, int rowsPerPage, String id,
            @Nullable ClickType... clickTypes) {
        this.plugin = Objects.requireNonNull(plugin);
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.rowsPerPage = rowsPerPage;
        this.id = id;

        this.buttons = new HashMap<>();
        this.stickySlots = new HashSet<>();

        this.currentPage = 0;

        this.permittedMenuClickTypes = clickTypes != null && clickTypes.length > 0
                ? new HashSet<>(Arrays.asList(clickTypes))
                : new HashSet<>(Arrays.asList(DEFAULT_PERMITTED_MENU_CLICK_TYPES));
    }

    // -- INVENTORY SETTINGS -- //

    /**
     * @param blockDefaultInteractions Whether the default behavior of click events
     *                                 should be cancelled.
     */
    public void setBlockDefaultInteractions(boolean blockDefaultInteractions) {
        this.blockDefaultInteractions = blockDefaultInteractions;
    }

    /**
     * @return Whether the default behavior of click events should be cancelled.
     */
    public boolean areDefaultInteractionsBlocked() {
        return blockDefaultInteractions;
    }

    /**
     * @param enableAutomaticPagination Whether pagination buttons should be
     *                                  automatically added.
     */
    public void setAutomaticPaginationEnabled(boolean enableAutomaticPagination) {
        this.enableAutomaticPagination = enableAutomaticPagination;
    }

    /**
     * @return Whether pagination buttons should be automatically added.
     */
    public Boolean isAutomaticPaginationEnabled() {
        return enableAutomaticPagination;
    }

    /**
     * @param toolbarBuilder The default toolbar builder used for GUIs.
     */
    public void setToolbarBuilder(GUIToolbarBuilder toolbarBuilder) {
        this.toolbarBuilder = toolbarBuilder;
    }

    /**
     * @return The default toolbar builder used for GUIs.
     */
    public GUIToolbarBuilder getToolbarBuilder() {
        return this.toolbarBuilder;
    }

    // -- INVENTORY OWNER -- //

    /**
     * Returns the plugin that the inventory is associated with. As this field is
     * final, this would be the plugin that
     * created the inventory.
     *
     * @return The plugin the inventory is associated with.
     */
    @Nonnull
    public JavaPlugin getPlugin() {
        return plugin;
    }

    // -- INVENTORY SIZE -- //

    /**
     * Returns the number of rows (of 9 columns) per page of the inventory. If you
     * want the total number of slots on a
     * page, you should use {@link #getPageSize()} instead.
     *
     * @return The number of rows per page.
     */
    public int getRowsPerPage() {
        return rowsPerPage;
    }

    /**
     * Returns the number of slots per page of the inventory. This would be
     * associated with the Bukkit/Spigot APIs
     * inventory 'size' parameter.
     *
     * <p>
     * So for example if {@link #getRowsPerPage()} was 3, this would be 27, as
     * Minecraft Chest inventories have rows
     * of 9 columns.
     *
     * @return The number of inventory slots per page.
     */
    public int getPageSize() {
        return rowsPerPage * 9;
    }

    /**
     * Sets the number of rows per page of the inventory.
     *
     * <p>
     * There is no way to set the number of slots per page directly, so if you need
     * to do that, you'll need to divide
     * the number of slots by 9 and supply the result to this parameter to achieve
     * that.
     *
     * @param rowsPerPage The number of rows per page.
     */
    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    // -- INVENTORY TAG -- //

    /**
     * This returns the GUI's id.
     *
     * @return The GUI's id.
     */
    public String getID() {
        return id;
    }

    /**
     * This sets the GUI's id.
     *
     * @see #getID()
     * @param id The GUI's id.
     */
    public void setID(String id) {
        this.id = id;
    }

    // -- INVENTORY TITLE -- //

    /**
     * This sets the inventory's display title.
     *
     * <p>
     * The title parameter is color code translated before the value is set. If you
     * want to avoid this behavior, you
     * should use {@link #setRawTitle(String)} which sets the inventory's title
     * directly.
     *
     * @param title The display title to set. (and to be color code translated)
     */
    public void setTitle(String title) {
        this.title = ChatColor.translateAlternateColorCodes('&', title);
    }

    /**
     * This sets the inventory's display title <b>without</b> first translating
     * color codes.
     *
     * @param title The display title to set.
     */
    public void setRawTitle(String title) {
        this.title = title;
    }

    /**
     * This returns the inventory's display title.
     *
     * <p>
     * Note that if you used {@link #setTitle(String)}, this will have been color
     * code translated already.
     *
     * @return The inventory's display title.
     */
    public String getTitle() {
        return title;
    }

    // -- BUTTONS -- //

    /**
     * Adds the provided {@link GUIButton}.
     *
     * @param button The button to add.
     */
    public void addButton(GUIButton button) {
        // If slot 0 is empty, but it's the 'highest filled slot', then set slot 0 to
        // contain button.
        // (This is an edge case for when the whole inventory is empty).
        if (getHighestFilledSlot() == 0 && getButton(0) == null) {
            setButton(0, button);
            return;
        }

        // Otherwise, add one to the highest filled slot, then use that slot for the new
        // button.
        setButton(getHighestFilledSlot() + 1, button);
    }

    /**
     * Adds the specified {@link GUIButton}s consecutively.
     *
     * @param buttons The buttons to add.
     */
    public void addButtons(GUIButton... buttons) {
        for (GUIButton button : buttons)
            addButton(button);
    }

    /**
     * Adds the provided {@link GUIButton} at the position denoted by the supplied
     * slot parameter.
     *
     * <p>
     * If you specify a value larger than the value of the first page, pagination
     * will be automatically applied when
     * the inventory is rendered. An alternative to this is to use
     * {@link #setButton(int, int, GUIButton)}.
     *
     * @see #setButton(int, int, GUIButton)
     * @param slot   The desired location of the button.
     * @param button The button to add.
     */
    public void setButton(int slot, GUIButton button) {
        buttons.put(slot, button);
    }

    /**
     * Adds the provided {@link GUIButton} at the position denoted by the supplied
     * slot parameter <i>on the page denoted
     * by the supplied page parameter</i>.
     *
     * <p>
     * This is an alias for {@link #setButton(int, GUIButton)}, however one where
     * the slot value is mapped to the
     * specified page. So if page is 2 (the third page) and the inventory row count
     * was 3 (so a size of 27), a supplied
     * slot value of 3 would actually map to a slot value of (2 * 27) + 3 = 54. The
     * mathematical formula for this is
     * <code>(page * pageSize) + slot</code>.
     *
     * <p>
     * If the slot value is out of the bounds of the specified page, this function
     * will do nothing.
     *
     * @see #setButton(int, GUIButton)
     * @param page   The page to which the button should be added.
     * @param slot   The position on that page the button should be added at.
     * @param button The button to add.
     */
    public void setButton(int page, int slot, GUIButton button) {
        if (slot < 0 || slot > getPageSize())
            return;

        setButton((page * getPageSize()) + slot, button);
    }

    /**
     * Removes a button from the specified slot.
     *
     * @param slot The slot containing the button you wish to remove.
     */
    public void removeButton(int slot) {
        buttons.remove(slot);
    }

    /**
     * An alias for {@link #removeButton(int)} to remove a button from the specified
     * slot on the specified page.
     *
     * <p>
     * If the slot value is out of the bounds of the specified page, this function
     * will do nothing.
     *
     * @param page The page containing the button you wish to remove.
     * @param slot The slot, of that page, containing the button you wish to remove.
     */
    public void removeButton(int page, int slot) {
        if (slot < 0 || slot > getPageSize())
            return;

        removeButton((page * getPageSize()) + slot);
    }

    /**
     * Returns the {@link GUIButton} in the specified slot.
     *
     * <p>
     * If you attempt to get a slot less than 0 or greater than the slot containing
     * the button at the greatest slot
     * value, this will return null.
     *
     * @param slot The slot containing the button you wish to get.
     * @return The {@link GUIButton} that was in that slot or null if the slot was
     *         invalid or if there was no button that
     *         slot.
     */
    public GUIButton getButton(int slot) {
        if (slot < 0 || slot > getHighestFilledSlot())
            return null;

        return buttons.get(slot);
    }

    /**
     * This is an alias for {@link #getButton(int)} that allows you to get a button
     * contained by a slot on a given page.
     *
     * @param page The page containing the button.
     * @param slot The slot, on that page, containing the button.
     * @return The {@link GUIButton} that was in that slot or null if the slot was
     *         invalid or if there was no button that
     *         slot.
     */
    public GUIButton getButton(int page, int slot) {
        if (slot < 0 || slot > getPageSize())
            return null;

        return getButton((page * getPageSize()) + slot);
    }

    // -- PAGINATION -- //

    /**
     * Returns the current page of the inventory. This is the page that will be
     * displayed when the inventory is opened
     * and displayed to a player (i.e. rendered).
     *
     * <p>
     * The value returned by {@code getCurrentPage} and accepted by
     * {@link #setCurrentPage(int)} is zero-indexed -
     * that is, the first page is 0. The analogue for getting the maximum page is
     * {@link #getMaxPageIndex()} (rather
     * than {@link #getMaxPage()} which is now deprecated).
     *
     * <p>
     * Unfortunately, the historic behavior for {@link #getMaxPage()} is confusingly
     * that it would be one-indexed not
     * zero-indexed - hence the deprecation (for clarity) - and it is anticipated
     * that simply changing the behavior of
     * that method would be subtle yet disastrous.
     *
     * @return The current page of the inventory.
     * @see #getMaxPageIndex()
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets the page of the inventory that will be displayed when the inventory is
     * opened and displayed to a player
     * (i.e. rendered).
     *
     * @param page The new current page of the inventory.
     */
    public void setCurrentPage(int page) {
        this.currentPage = page;
        if (this.onPageChange != null)
            this.onPageChange.accept(this);
    }

    /**
     * Gets the page number of the final page of the GUI.
     *
     * <p>
     * This method is now an alias for {@link #getMaxPageNumber()}.
     *
     * @return The highest page number that can be viewed.
     * @deprecated this method is ambiguous with regard to indexing (confusingly,
     *             this method is the one-indexed value
     *             for the maximum page whilst {@link #getCurrentPage()} is the
     *             zero-indexed value) - use
     *             {@link #getMaxPageIndex()} or {@link #getMaxPageNumber()} (the
     *             latter has the same behavior) instead to
     *             receive the expected value.
     */
    @Deprecated
    public int getMaxPage() {
        return getMaxPageNumber();
    }

    /**
     * Get the maximum page index that can be displayed (starting at 0).
     *
     * <p>
     * For example, an empty inventory would have a maximum page index of 0 and a
     * maximum page number of 1.
     *
     * @return the maximum page index.
     * @see #getMaxPageNumber()
     */
    public int getMaxPageIndex() {
        return getMaxPageNumber() - 1;
    }

    /**
     * Get the maximum page (natural) number that can be displayed (starting at 1).
     *
     * @return the maximum page number.
     * @see #getMaxPageIndex()
     */
    public int getMaxPageNumber() {
        return (int) Math.ceil(((double) getHighestFilledSlot() + 1) / ((double) getPageSize()));
    }

    /**
     * Returns the slot number of the highest filled slot. This is mainly used to
     * calculate the number of pages there
     * needs to be to display the GUI's contents in the rendered inventory.
     *
     * @return The highest filled slot's number.
     */
    public int getHighestFilledSlot() {
        int slot = 0;

        for (int nextSlot : buttons.keySet()) {
            if (buttons.get(nextSlot) != null && nextSlot > slot)
                slot = nextSlot;
        }

        return slot;
    }

    /**
     * Increments the current page. This will automatically refresh the inventory by
     * calling
     * {@link #refreshInventory(HumanEntity)} if the page was changed.
     *
     * @param viewer The {@link HumanEntity} viewing the inventory.
     * @return Whether the page could be changed (false means the max page is
     *         currently open).
     */
    public boolean nextPage(HumanEntity viewer) {
        if (currentPage < getMaxPageIndex()) {
            currentPage++;
            refreshInventory(viewer);
            if (this.onPageChange != null)
                this.onPageChange.accept(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Decrements the current page. This will automatically refresh the inventory by
     * calling
     * {@link #refreshInventory(HumanEntity)} if the page was changed.
     *
     * @param viewer The {@link HumanEntity} viewing the inventory.
     * @return Whether the page could be changed (false means the first page is
     *         currently open).
     */
    public boolean previousPage(HumanEntity viewer) {
        if (currentPage > 0) {
            currentPage--;
            refreshInventory(viewer);
            if (this.onPageChange != null)
                this.onPageChange.accept(this);
            return true;
        } else {
            return false;
        }
    }

    // -- STICKY SLOTS -- //

    /**
     * Marks a slot as 'sticky', so that when the page is changed, the slot will
     * always display the value on the first
     * page.
     *
     * <p>
     * This is useful for implementing things like 'toolbars', where you have a set
     * of common items on every page.
     *
     * <p>
     * If the slot is out of the bounds of the first page (i.e. less than 0 or
     * greater than {@link #getPageSize()} -
     * 1) this method will do nothing.
     *
     * @param slot The slot to mark as 'sticky'.
     */
    public void stickSlot(int slot) {
        if (slot < 0 || slot >= getPageSize())
            return;

        this.stickySlots.add(slot);
    }

    /**
     * Un-marks a slot as sticky - thereby meaning that slot will display whatever
     * its value on the current page is.
     *
     * @see #stickSlot(int)
     * @param slot The slot to un-mark as 'sticky'.
     */
    public void unstickSlot(int slot) {
        this.stickySlots.remove(slot);
    }

    /**
     * This clears all the 'stuck' slots - essentially un-marking all stuck slots.
     *
     * @see #stickSlot(int)
     */
    public void clearStickySlots() {
        this.stickySlots.clear();
    }

    /**
     * This checks whether a given slot is sticky. If the slot is out of bounds of
     * the first page (as defined by the
     * same parameters as {@link #stickSlot(int)}), this will return false.
     *
     * @see #stickSlot(int)
     * @param slot The slot to check.
     * @return True if the slot is sticky, false if it isn't or the slot was out of
     *         bounds.
     */
    public boolean isStickiedSlot(int slot) {
        if (slot < 0 || slot >= getPageSize())
            return false;

        return this.stickySlots.contains(slot);
    }

    /**
     * This clears all slots in the inventory, except those which have been marked
     * as 'sticky'.
     *
     * @see #stickSlot(int)
     */
    public void clearAllButStickiedSlots() {
        this.currentPage = 0;
        buttons.entrySet().removeIf(button -> !isStickiedSlot(button.getKey()));
    }

    // -- EVENTS -- //

    /**
     * The action to be performed on close.
     *
     * @return The action to be performed on close.
     * @see #setOnClose(Consumer)
     */
    public Consumer<InventoryCloseEvent> getOnClose() {
        return this.onClose;
    }

    /**
     * Used to set an action to be performed on inventory close without registering
     * an
     * {@link org.bukkit.event.inventory.InventoryCloseEvent} specifically for this
     * inventory.
     *
     * @param onClose The action to be performed on close.
     */
    public void setOnClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
    }

    /**
     * The action to be performed on page change.
     *
     * @return The action to be performed on page change.
     * @see #setOnPageChange(Consumer)
     */
    public Consumer<GUIMenu> getOnPageChange() {
        return this.onPageChange;
    }

    /**
     * Used to set an action to be performed on inventory page change.
     *
     * @param onPageChange The action to be performed on page change.
     */
    public void setOnPageChange(Consumer<GUIMenu> onPageChange) {
        this.onPageChange = onPageChange;
    }

    /**
     * Returns the permitted menu click types.
     *
     * @return A hashSet of permitted menu click types
     */
    public HashSet<ClickType> getPermittedMenuClickTypes() {
        return this.permittedMenuClickTypes;
    }

    /**
     * Returns an array of blocked menu actions for the current Inventory.
     *
     * @return A hashSet of blocked menu actions
     */
    public HashSet<InventoryAction> getBlockedMenuActions() {
        return this.blockedMenuActions;
    }

    /**
     * Returns the blocked adjacent actions for this object.
     *
     * @return A hashSet of InventoryAction objects representing the blocked
     *         adjacent actions.
     */
    public HashSet<InventoryAction> getBlockedAdjacentActions() {
        return this.blockedAdjacentActions;
    }

    /**
     * Sets the permitted menu click types.
     *
     * @param clickTypes One or more click types you want to allow for this menu.
     */
    public void setPermittedMenuClickTypes(ClickType... clickTypes) {
        this.permittedMenuClickTypes = new HashSet<>(Arrays.asList(clickTypes));
    }

    /**
     * Sets the blocked menu actions for the inventory.
     *
     * @param actions the menu actions to be blocked
     */
    public void setBlockedMenuActions(InventoryAction... actions) {
        this.blockedMenuActions = new HashSet<>(Arrays.asList(actions));
    }

    /**
     * Sets the blocked adjacent actions for this object.
     *
     * @param actions The actions to be blocked.
     */
    public void setBlockedAdjacentActions(InventoryAction... actions) {
        this.blockedAdjacentActions = new HashSet<>(Arrays.asList(actions));
    }

    /**
     * Adds a permitted click type to the menu.
     *
     * @param clickType the click type to be added
     */
    public void addPermittedClickType(ClickType clickType) {
        this.permittedMenuClickTypes.add(clickType);
    }

    /**
     * Adds the given InventoryAction to the list of blocked menu actions. Blocked
     * menu actions are actions that are not
     * allowed to be performed on the inventory menu.
     *
     * @param action The InventoryAction to be added to the blocked menu actions
     *               list.
     */
    public void addBlockedMenuAction(InventoryAction action) {
        this.blockedMenuActions.add(action);
    }

    /**
     * Adds a blocked adjacent action to the list of blocked adjacent actions.
     *
     * @param action The inventory action to be added as blocked adjacent action.
     */
    public void addBlockedAdjacentAction(InventoryAction action) {
        this.getBlockedAdjacentActions().add(action);
    }

    /**
     * Removes a permitted click type from the list of permitted menu click types.
     *
     * @param clickType the click type to be removed
     */
    public void removePermittedClickType(ClickType clickType) {
        this.permittedMenuClickTypes.remove(clickType);
    }

    /**
     * Removes the specified InventoryAction from the list of blocked menu actions.
     *
     * @param action the InventoryAction to be removed
     */
    public void removeBlockedMenuAction(InventoryAction action) {
        this.blockedMenuActions.remove(action);
    }

    /**
     * Removes the given action from the list of blocked adjacent actions.
     *
     * @param action The action to be removed
     */
    public void removeBlockedAdjacentAction(InventoryAction action) {
        this.getBlockedAdjacentActions().remove(action);
    }

    // -- INVENTORY API -- //

    /**
     * Refresh an inventory that is currently open for a given viewer.
     *
     * <p>
     * This method checks if the specified viewer is looking at an {@link GUIMenu}
     * and, if they are, it refreshes the
     * inventory for them.
     *
     * @param viewer The viewer of the open inventory.
     */
    public void refreshInventory(HumanEntity viewer) {
        // If the open inventory isn't an GUIMenu - or if it isn't this inventory, do
        // nothing.
        if (!(viewer.getOpenInventory().getTopInventory().getHolder() instanceof GUIMenu)
                || viewer.getOpenInventory().getTopInventory().getHolder() != this)
            return;

        // If the new size is different, we'll need to open a new inventory.
        if (viewer.getOpenInventory().getTopInventory().getSize() != getPageSize() + (getMaxPageNumber() > 0 ? 9 : 0)) {
            viewer.openInventory(getInventory());
            return;
        }

        // If the title has changed, we'll need to open a new inventory.
        String newTitle = title.replace("{currentPage}", String.valueOf(currentPage + 1))
                .replace("{maxPage}", String.valueOf(getMaxPageNumber()));
        if (!viewer.getOpenInventory().getTitle().equals(newTitle)) {
            viewer.openInventory(getInventory());
            return;
        }

        // Otherwise, we can refresh the contents without re-opening the inventory.
        viewer.getOpenInventory().getTopInventory().setContents(getInventory().getContents());
    }

    /**
     * Returns the Bukkit/Spigot {@link Inventory} that represents the GUI. This is
     * shown to a player using
     * {@link HumanEntity#openInventory(Inventory)}.
     *
     * @return The created inventory used to display the GUI.
     */
    @Override
    public Inventory getInventory() {
        boolean needsPagination = getMaxPageNumber() > 0 && isAutomaticPaginationEnabled();

        Inventory inventory = Bukkit.createInventory(
                this,
                ((needsPagination)
                        // Pagination enabled: add the bottom toolbar row.
                        ? getPageSize() + 9
                        // Pagination not required or disabled.
                        : getPageSize()),
                title.replace("{currentPage}", String.valueOf(currentPage + 1))
                        .replace("{maxPage}", String.valueOf(getMaxPageNumber())));

        // Add the main inventory items.
        for (int key = currentPage * getPageSize(); key < (currentPage + 1) * getPageSize(); key++) {
            // If we've already reached the maximum assigned slot, stop assigning
            // slots.
            if (key > getHighestFilledSlot())
                break;

            if (buttons.containsKey(key)) {
                inventory.setItem(
                        key - (currentPage * getPageSize()), buttons.get(key).getIcon());
            }
        }

        // Update the stickied slots.
        for (int stickiedSlot : stickySlots) {
            inventory.setItem(stickiedSlot, buttons.get(stickiedSlot).getIcon());
        }

        // Render the pagination items.
        if (needsPagination) {
            GUIToolbarBuilder toolbarButtonBuilder = getToolbarBuilder();

            int pageSize = getPageSize();
            for (int i = pageSize; i < pageSize + 9; i++) {
                int offset = i - pageSize;

                GUIButton paginationButton = toolbarButtonBuilder.buildToolbarButton(
                        offset, getCurrentPage(), GUIToolbarButtonType.getDefaultForSlot(offset), this);
                inventory.setItem(i, paginationButton != null ? paginationButton.getIcon() : null);
            }
        }

        return inventory;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GUIMenu.class.getSimpleName() + "[", "]")
                .add("title='" + title + "'")
                .add("id='" + id + "'")
                .add("rowsPerPage=" + rowsPerPage)
                .add("currentPage=" + currentPage)
                .toString();
    }

}

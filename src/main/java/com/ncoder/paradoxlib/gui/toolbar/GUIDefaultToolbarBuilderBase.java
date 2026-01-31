package com.ncoder.paradoxlib.gui.toolbar;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.bukkit.event.Event;

import com.ncoder.paradoxlib.gui.GUIMenu;
import com.ncoder.paradoxlib.gui.button.GUIButton;
import com.ncoder.paradoxlib.gui.item.ItemBuilder;

/**
 * The default base implementation of {@link GUIToolbarBuilder}.
 *
 * <p>
 * This class is the base of the class used by GUIMenu as a default
 * implementation. You can build your own custom
 * toolbar by implementing {@link GUIToolbarBuilder} and passing your custom
 * implementation to
 * {@link GUIMenu#setToolbarBuilder(GUIToolbarBuilder)}).
 */
@Immutable
public abstract class GUIDefaultToolbarBuilderBase implements GUIToolbarBuilder {
    /** A label equivalent to "&lt;- Previous Page". */
    private final NameBuilder previousPageLabelBuilder;

    /** An additional description displayed under {@code previousPage}. */
    private final LoreBuilder previousPageDescriptionBuilder;

    /** A label equivalent to "Page X of Y" */
    private final NameBuilder currentPageLabelBuilder;

    /** An additional description displayed under {@code currentPage}. */
    private final LoreBuilder currentPageDescriptionBuilder;

    /** A label equivalent to "Next Page -&gt;". */
    private final NameBuilder nextPageLabelBuilder;

    /** An additional description displayed under {@code nextPage}. */
    private final LoreBuilder nextPageDescriptionBuilder;

    /**
     * Construct the base default implementation of {@link GUIToolbarBuilder}.
     *
     * @param previousPageLabelBuilder       A label equivalent to "&lt;- Previous
     *                                       Page".
     * @param previousPageDescriptionBuilder An additional description displayed
     *                                       under {@code previousPage}.
     * @param currentPageLabelBuilder        A label equivalent to "Page X of Y"
     * @param currentPageDescriptionBuilder  An additional description displayed
     *                                       under {@code currentPage}.
     * @param nextPageLabelBuilder           A label equivalent to "Next Page
     *                                       -&gt;".
     * @param nextPageDescriptionBuilder     An additional description displayed
     *                                       under {@code nextPage}.
     */
    public GUIDefaultToolbarBuilderBase(
            final NameBuilder previousPageLabelBuilder,
            final LoreBuilder previousPageDescriptionBuilder,
            final NameBuilder currentPageLabelBuilder,
            final LoreBuilder currentPageDescriptionBuilder,
            final NameBuilder nextPageLabelBuilder,
            final LoreBuilder nextPageDescriptionBuilder) {
        this.previousPageLabelBuilder = previousPageLabelBuilder;
        this.previousPageDescriptionBuilder = previousPageDescriptionBuilder;
        this.currentPageLabelBuilder = currentPageLabelBuilder;
        this.currentPageDescriptionBuilder = currentPageDescriptionBuilder;
        this.nextPageLabelBuilder = nextPageLabelBuilder;
        this.nextPageDescriptionBuilder = nextPageDescriptionBuilder;
    }

    /**
     * Construct the base default implementation of {@link GUIToolbarBuilder} with
     * default options.
     */
    public GUIDefaultToolbarBuilderBase() {
        this(
                // Previous
                menu -> "&a&l← Previous Page",
                menu -> Arrays.asList("&aClick to move back to", "&apage " + menu.getCurrentPage() + "."),

                // Current
                menu -> String.format("&7&lPage %d of %d", menu.getCurrentPage() + 1, menu.getMaxPageNumber()),
                menu -> Arrays.asList("&7You are currently viewing", "&7page " + (menu.getCurrentPage() + 1) + "."),

                // Next
                menu -> "&a&lNext Page →",
                menu -> Arrays.asList("&aClick to move forward to", "&apage " + (menu.getCurrentPage() + 2) + "."));
    }

    /**
     * A label equivalent to "&lt;- Previous Page".
     *
     * @return A builder for a label equivalent to "&lt;- Previous Page".
     */
    public NameBuilder getPreviousPageLabelBuilder() {
        return this.previousPageLabelBuilder;
    }

    /**
     * An additional description displayed under {@code previousPage}.
     *
     * @return A builder for an additional description displayed under
     *         {@code previousPage}.
     */
    public LoreBuilder getPreviousPageDescriptionBuilder() {
        return this.previousPageDescriptionBuilder;
    }

    /**
     * A label equivalent to "Page X of Y"
     *
     * @return A builder for a label equivalent to "Page X of Y"
     */
    public NameBuilder getCurrentPageLabelBuilder() {
        return this.currentPageLabelBuilder;
    }

    /**
     * An additional description displayed under {@code currentPage}.
     *
     * @return A builder for an additional description displayed under
     *         {@code currentPage}.
     */
    public LoreBuilder getCurrentPageDescriptionBuilder() {
        return this.currentPageDescriptionBuilder;
    }

    /**
     * A label equivalent to "Next Page -&gt;".
     *
     * @return A builder for a label equivalent to "Next Page -&gt;".
     */
    public NameBuilder getNextPageLabelBuilder() {
        return this.nextPageLabelBuilder;
    }

    /**
     * An additional description displayed under {@code nextPage}.
     *
     * @return A builder for an additional description displayed under
     *         {@code nextPage}.
     */
    public LoreBuilder getNextPageDescriptionBuilder() {
        return this.nextPageDescriptionBuilder;
    }

    @Nullable
    @Override
    public GUIButton buildToolbarButton(
            int slot, int page, @Nonnull GUIToolbarButtonType defaultType, @Nonnull GUIMenu menu) {
        if (page != menu.getCurrentPage()) {
            throw new IllegalStateException(
                    "Toolbar fragment page number is not consistent with menu page. (This may be caused by an unexpected concurrent modification - please open an issue on GitHub).");
        }

        switch (defaultType) {
            case PREV_BUTTON:
                if (menu.getCurrentPage() > 0)
                    return new GUIButton(Objects.requireNonNull(initializePreviousPageButton())
                            .name(Objects.requireNonNull(this.previousPageLabelBuilder.buildName(menu)))
                            .lore(Objects.requireNonNull(this.previousPageDescriptionBuilder.buildLore(menu)))
                            .build())
                            .withListener(event -> {
                                event.setResult(Event.Result.DENY);
                                menu.previousPage(event.getWhoClicked());
                            });
                else
                    return null;

            case CURRENT_BUTTON:
                return new GUIButton(Objects.requireNonNull(initializeCurrentPageIndicator())
                        .name(Objects.requireNonNull(this.currentPageLabelBuilder.buildName(menu)))
                        .lore(Objects.requireNonNull(this.currentPageDescriptionBuilder.buildLore(menu)))
                        .build())
                        .withListener(event -> event.setResult(Event.Result.DENY));

            case NEXT_BUTTON:
                if (menu.getCurrentPage() < menu.getMaxPageIndex())
                    return new GUIButton(Objects.requireNonNull(initializeNextPageButton())
                            .name(Objects.requireNonNull(this.nextPageLabelBuilder.buildName(menu)))
                            .lore(Objects.requireNonNull(this.nextPageDescriptionBuilder.buildLore(menu)))
                            .build())
                            .withListener(event -> {
                                event.setResult(Event.Result.DENY);
                                menu.nextPage(event.getWhoClicked());
                            });
                else
                    return null;

            case UNASSIGNED:
            default:
                return null;
        }
    }

    /**
     * Build the initial previous page button.
     *
     * <p>
     * The primary purpose of this method is to initialize an ItemBuilder with an
     * icon. This allows version-specific
     * materials and {@link ItemBuilder} implementation to be used.
     *
     * <p>
     * The item will then be further customized with name, listener, etc., and
     * subsequently built within the
     * {@link GUIDefaultToolbarBuilderBase}.
     *
     * @return the previous page button.
     */
    @Nonnull
    protected abstract ItemBuilder initializePreviousPageButton();

    /**
     * Build the initial current page button.
     *
     * <p>
     * The primary purpose of this method is to initialize an ItemBuilder with an
     * icon. This allows version-specific
     * materials and {@link ItemBuilder} implementation to be used.
     *
     * <p>
     * The item will then be further customized with name, listener, etc., and
     * subsequently built within the
     * {@link GUIDefaultToolbarBuilderBase}.
     *
     * @return the current page button.
     */
    @Nonnull
    protected abstract ItemBuilder initializeCurrentPageIndicator();

    /**
     * Build the initial next page button.
     *
     * <p>
     * The primary purpose of this method is to initialize an ItemBuilder with an
     * icon. This allows version-specific
     * materials and {@link ItemBuilder} implementation to be used.
     *
     * <p>
     * The item will then be further customized with name, listener, etc., and
     * subsequently built within the
     * {@link GUIDefaultToolbarBuilderBase}.
     *
     * @return the next page button.
     */
    @Nonnull
    protected abstract ItemBuilder initializeNextPageButton();
}

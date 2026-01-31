package com.ncoder.paradoxlib.gui.toolbar;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GUIDefaultToolbarBuilderFactory {

    static {
        // See ItemBuilderFactory.
        FACTORY = new GUIDefaultToolbarBuilderFactory();
    }

    /** The static singleton {@link GUIDefaultToolbarBuilderFactory} instance. */
    @Nonnull
    private static final GUIDefaultToolbarBuilderFactory FACTORY;

    @Nullable
    private Supplier<GUIToolbarBuilder> supplier;

    /**
     * Internal default constructor for the {@link GUIDefaultToolbarBuilderFactory}.
     */
    private GUIDefaultToolbarBuilderFactory() {
    }

    /**
     * Get the globally registered instance of the
     * {@link GUIDefaultToolbarBuilderFactory}.
     *
     * @return the {@link GUIDefaultToolbarBuilderFactory}.
     */
    public static GUIDefaultToolbarBuilderFactory get() {
        return Objects.requireNonNull(FACTORY);
    }

    /**
     * Checks whether the supplier has been registered elsewhere with
     * {@link #setSupplier(Supplier)}.
     *
     * @return true if the supplier has already been registered (implying new ones
     *         will be ignored).
     */
    public boolean hasSupplier() {
        return this.supplier != null;
    }

    /**
     * Set the supplier for the {@link GUIDefaultToolbarBuilderFactory}.
     *
     * @param supplier to use when creating an {@link GUIToolbarBuilder}.
     */
    public void setSupplier(@Nonnull Supplier<GUIToolbarBuilder> supplier) {
        if (hasSupplier()) {
            return;
        }

        this.supplier = Objects.requireNonNull(supplier);
    }

    /**
     * Instantiate a new {@link GUIToolbarBuilder} using the supplier passed to
     * {@link #setSupplier(Supplier)}.
     *
     * @return supplier to use for creating {@link GUIToolbarBuilder}s.
     */
    @Nonnull
    public GUIToolbarBuilder newToolbarBuilder() {
        final Supplier<GUIToolbarBuilder> supplier = Objects.requireNonNull(
                this.supplier, "The GUIDefaultToolbarBuilderFactory has not been configured with #setSupplier yet.");
        return Objects.requireNonNull(
                supplier.get(),
                "The supplier returned a null GUIToolbarBuilder which is not permitted. This means whatever has called #setSupplier first has provided an invalid supplier.");
    }

}

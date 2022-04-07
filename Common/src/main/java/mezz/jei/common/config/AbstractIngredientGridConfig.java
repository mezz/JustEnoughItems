package mezz.jei.common.config;

import mezz.jei.common.gui.overlay.options.NavigationVisibility;
import mezz.jei.common.gui.overlay.options.VerticalAlignment;

public abstract class AbstractIngredientGridConfig implements IIngredientGridConfig {
    protected static final int minNumRows = 1;
    protected static final int defaultNumRows = 16;
    protected static final int largestNumRows = 100;

    protected static final int minNumColumns = 4;
    protected static final int defaultNumColumns = 9;
    protected static final int largestNumColumns = 100;

    protected static final VerticalAlignment defaultVerticalAlignment = VerticalAlignment.TOP;
    protected static final NavigationVisibility defaultButtonNavigationVisibility = NavigationVisibility.ENABLED;
    protected static final boolean defaultDrawBackground = false;
}

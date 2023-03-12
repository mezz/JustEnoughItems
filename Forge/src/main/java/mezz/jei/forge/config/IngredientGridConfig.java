package mezz.jei.forge.config;

import mezz.jei.api.config.IIngredientGridConfig;
import mezz.jei.api.config.gui.HorizontalAlignment;
import mezz.jei.api.config.gui.NavigationVisibility;
import mezz.jei.api.config.gui.VerticalAlignment;
import net.minecraftforge.common.ForgeConfigSpec;

public class IngredientGridConfig implements IIngredientGridConfig {
    private final ForgeConfigSpec.IntValue maxRows;
    private final ForgeConfigSpec.IntValue maxColumns;
    private final ForgeConfigSpec.EnumValue<HorizontalAlignment> alignmentHorizontal;
    private final ForgeConfigSpec.EnumValue<VerticalAlignment> alignmentVertical;
    private final ForgeConfigSpec.EnumValue<NavigationVisibility> navigationVis;
    private final ForgeConfigSpec.BooleanValue drawBackground;

    public IngredientGridConfig(ForgeConfigSpec.Builder builder, HorizontalAlignment defaultHorizontalAlignment) {
        builder.comment("Max number of rows shown");
        maxRows = builder.defineInRange(
                "max_rows",
                defaultNumRows,
                minNumRows,
                largestNumRows
                );

        builder.comment("Max number of columns shown");
        maxColumns = builder.defineInRange(
                "max_columns",
                defaultNumColumns,
                minNumColumns,
                largestNumColumns
        );

        builder.comment("Horizontal alignment of the ingredient grid inside the available area");
        alignmentHorizontal = builder.defineEnum("horizontal_alignment", defaultHorizontalAlignment);

        builder.comment("Vertical alignment of the ingredient grid inside the available area");
        alignmentVertical = builder.defineEnum("vertical_alignment", defaultVerticalAlignment);

        builder.comment("Visibility of the top page buttons. Use AUTO_HIDE to only show it when there are multiple pages.");
        navigationVis = builder.defineEnum("navigation_visibility", defaultButtonNavigationVisibility);

        builder.comment("Set to true to draw a background texture behind the gui.");
        drawBackground = builder.define("draw_background", defaultDrawBackground);
    }

    @Override
    public int getMaxColumns() {
        return maxColumns.get();
    }

    @Override
    public int getMinColumns() {
        return minNumColumns;
    }

    @Override
    public int getMaxRows() {
        return maxRows.get();
    }

    @Override
    public int getMinRows() {
        return minNumColumns;
    }

    @Override
    public boolean drawBackground() {
        return drawBackground.get();
    }

    @Override
    public HorizontalAlignment getHorizontalAlignment() {
        return alignmentHorizontal.get();
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        return alignmentVertical.get();
    }

    @Override
    public NavigationVisibility getButtonNavigationVisibility() {
        return navigationVis.get();
    }
}

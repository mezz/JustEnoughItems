package mezz.jei.config;

import mezz.jei.gui.overlay.BackgroundType;
import mezz.jei.gui.overlay.HorizontalAlignment;
import mezz.jei.gui.overlay.NavigationVisibility;
import mezz.jei.gui.overlay.VerticalAlignment;
import mezz.jei.util.ImmutableRect2i;
import net.minecraftforge.common.ForgeConfigSpec;

public class IngredientGridConfig implements IIngredientGridConfig {
	private static final int minNumRows = 1;
	private static final int defaultNumRows = 16;
	private static final int largestNumRows = 100;

	private static final int minNumColumns = 4;
	private static final int defaultNumColumns = 9;
	private static final int largestNumColumns = 100;

	private final ForgeConfigSpec.IntValue maxRows;
	private final ForgeConfigSpec.IntValue maxColumns;
	private final ForgeConfigSpec.EnumValue<HorizontalAlignment> horizontalAlignment;
	private final ForgeConfigSpec.EnumValue<VerticalAlignment> verticalAlignment;
	private final ForgeConfigSpec.EnumValue<NavigationVisibility> buttonNavigationVisibility;
	private final ForgeConfigSpec.EnumValue<BackgroundType> backgroundType;

	public IngredientGridConfig(String categoryName, ForgeConfigSpec.Builder builder, HorizontalAlignment defaultHorizontalAlignment) {
		builder.push(categoryName);
		{
			builder.comment("Max number of rows shown");
			maxRows = builder.defineInRange("MaxRows", defaultNumRows, minNumRows, largestNumRows);

			builder.comment("Max number of columns shown");
			maxColumns = builder.defineInRange("MaxColumns", defaultNumColumns, minNumColumns, largestNumColumns);

			builder.comment("Horizontal alignment of the ingredient grid inside the available area");
			horizontalAlignment = builder.defineEnum("HorizontalAlignment", defaultHorizontalAlignment);

			builder.comment("Horizontal alignment of the ingredient grid inside the available area");
			verticalAlignment = builder.defineEnum("VerticalAlignment", VerticalAlignment.TOP);

			builder.comment("Visibility of the top page buttons. Use AUTO_HIDE to only show it when there are multiple pages.");
			buttonNavigationVisibility = builder.defineEnum("ButtonNavigationVisibility", NavigationVisibility.ENABLED);

			builder.comment("The type of background drawn behind the gui.");
			backgroundType = builder.defineEnum("BackgroundType", BackgroundType.NONE);
		}
		builder.pop();
	}

	@Override
	public int getMinColumns() {
		return minNumColumns;
	}

	@Override
	public int getMinRows() {
		return minNumRows;
	}

	@Override
	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment.get();
	}

	@Override
	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment.get();
	}

	@Override
	public BackgroundType getBackgroundType() {
		return backgroundType.get();
	}

	@Override
	public int getMaxColumns() {
		return maxColumns.get();
	}

	@Override
	public int getMaxRows() {
		return maxRows.get();
	}

	@Override
	public NavigationVisibility getButtonNavigationVisibility() {
		return buttonNavigationVisibility.get();
	}

	@Override
	public ImmutableRect2i calculateBounds(ImmutableRect2i availableArea, int ingredientWidth, int ingredientHeight) {
		final int columns = Math.min(availableArea.getWidth() / ingredientWidth, getMaxColumns());
		final int rows = Math.min(availableArea.getHeight() / ingredientHeight, getMaxRows());
		if (rows < getMinRows() || columns < getMinColumns()) {
			return ImmutableRect2i.EMPTY;
		}

		final int width = columns * ingredientWidth;
		final int height = rows * ingredientHeight;
		final int x = switch (getHorizontalAlignment()) {
			case LEFT -> availableArea.getX();
			case CENTER -> availableArea.getX() + ((availableArea.getWidth() - width) / 2);
			case RIGHT -> availableArea.getX() + (availableArea.getWidth() - width);
		};
		final int y = switch (getVerticalAlignment()) {
			case TOP -> availableArea.getY();
			case CENTER -> availableArea.getY() + ((availableArea.getHeight() - height) / 2);
			case BOTTOM -> availableArea.getY() + (availableArea.getHeight() - height);
		};

		return new ImmutableRect2i(x, y, width, height);
	}
}

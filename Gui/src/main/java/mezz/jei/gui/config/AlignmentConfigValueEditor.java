package mezz.jei.gui.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.mezzdev.config.gui.api.ConfigInfo;
import net.mezzdev.config.gui.api.ConfigValueLocalization;
import net.mezzdev.config.gui.api.IConfigScreenValue;
import net.mezzdev.config.gui.api.IConfigValueEditor;
import net.mezzdev.config.gui.api.IConfigValuePopup;
import net.mezzdev.config.gui.api.LegacyGuiGraphics;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

/**
 * Custom compact editor for JEI's combined alignment config value.
 */
final class AlignmentConfigValueEditor implements IConfigValueEditor<Alignment> {
	private static final int CONTROL_WIDTH = 24;
	private static final int CONTROL_HEIGHT = 24;
	private static final int GRID_SIZE = 3;

	@Override
	public int getControlWidth(IConfigScreenValue<Alignment> configValue, Alignment value) {
		return CONTROL_WIDTH;
	}

	@Override
	public int getControlHeight(IConfigScreenValue<Alignment> configValue, Alignment value) {
		return CONTROL_HEIGHT;
	}

	@Override
	public void draw(
		LegacyGuiGraphics guiGraphics,
		Rect2i area,
		IConfigScreenValue<Alignment> configValue,
		Alignment value,
		boolean hovered,
		boolean hasPendingChange
	) {
		AlignmentGridRenderer.drawIcon(guiGraphics, area, value);
	}

	@Override
	public Optional<ConfigInfo> getTooltipInfo(
		Rect2i area,
		IConfigScreenValue<Alignment> configValue,
		Alignment value,
		boolean hasPendingChange,
		double mouseX,
		double mouseY
	) {
		return getAlignmentAt(area, mouseX, mouseY)
			.map(alignment -> new ConfigInfo(
				ConfigValueLocalization.getName(configValue),
				ConfigValueLocalization.getValueName(configValue, alignment)
			));
	}

	@Override
	public Optional<IConfigValuePopup<Alignment>> createPopup(
		Rect2i area,
		IConfigScreenValue<Alignment> configValue,
		Alignment value,
		double mouseX,
		double mouseY,
		int button
	) {
		if (button != 0) {
			return Optional.empty();
		}
		return Optional.of(new AlignmentSelectorPopup(value));
	}

	private static Optional<Alignment> getAlignmentAt(Rect2i area, double mouseX, double mouseY) {
		if (!area.contains((int) mouseX, (int) mouseY)) {
			return Optional.empty();
		}
		int column = getIndex(area.getX(), area.getWidth(), mouseX);
		int row = getIndex(area.getY(), area.getHeight(), mouseY);
		return Optional.of(Alignment.from(getHorizontalAlignment(column), getVerticalAlignment(row)));
	}

	private static int getIndex(int start, int length, double value) {
		double relative = value - start;
		int index = (int) (relative * GRID_SIZE / length);
		return Math.max(0, Math.min(index, GRID_SIZE - 1));
	}

	private static HorizontalAlignment getHorizontalAlignment(int column) {
		return switch (column) {
			case 0 -> HorizontalAlignment.LEFT;
			case 1 -> HorizontalAlignment.CENTER;
			default -> HorizontalAlignment.RIGHT;
		};
	}

	private static VerticalAlignment getVerticalAlignment(int row) {
		return switch (row) {
			case 0 -> VerticalAlignment.TOP;
			case 1 -> VerticalAlignment.CENTER;
			default -> VerticalAlignment.BOTTOM;
		};
	}
}

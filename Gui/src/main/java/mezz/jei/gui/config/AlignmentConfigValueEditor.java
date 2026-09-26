package mezz.jei.gui.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.mezzdev.config.gui.api.ConfigInfo;
import net.mezzdev.config.gui.api.ConfigValueLocalization;
import net.mezzdev.config.gui.api.IConfigScreenValue;
import net.mezzdev.config.gui.api.IConfigValueEditor;
import net.mezzdev.config.gui.api.IConfigValuePopup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

/**
 * Custom compact editor for JEI's combined alignment config value.
 */
final class AlignmentConfigValueEditor implements IConfigValueEditor<Alignment> {
	private static final int CONTROL_WIDTH = 24;
	private static final int CONTROL_HEIGHT = 24;

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
		GuiGraphicsExtractor guiGraphics,
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
		if (!area.contains((int) mouseX, (int) mouseY)) {
			return Optional.empty();
		}
		return Optional.of(new ConfigInfo(
			ConfigValueLocalization.getName(configValue),
			ConfigValueLocalization.getValueName(configValue, value)
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
		if (button != InputConstants.MOUSE_BUTTON_LEFT) {
			return Optional.empty();
		}
		return Optional.of(new AlignmentSelectorPopup(value));
	}
}

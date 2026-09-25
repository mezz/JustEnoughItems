package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IngredientGridBackgroundStyle;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.mezzdev.config.api.value.IConfigValue;

import java.util.List;
import java.util.Set;

/**
 * Draws ingredient-grid panel backgrounds, slot backgrounds, and exclusion-area shadows.
 */
public final class IngredientGridBackgroundRenderer {
	private final IConfigValue<IngredientGridBackgroundStyle> backgroundStyle;
	private final ScalableDrawable background;
	private final ScalableDrawable slotBackground;
	private final ScalableDrawable exclusionAreaShadow;

	public IngredientGridBackgroundRenderer(
		IConfigValue<IngredientGridBackgroundStyle> backgroundStyle,
		ScalableDrawable background,
		ScalableDrawable slotBackground,
		ScalableDrawable exclusionAreaShadow
	) {
		this.backgroundStyle = backgroundStyle;
		this.background = background;
		this.slotBackground = slotBackground;
		this.exclusionAreaShadow = exclusionAreaShadow;
	}

	public void draw(
		GuiGraphicsExtractor guiGraphics,
		List<Panel> panels,
		Set<ImmutableRect2i> guiExclusionAreas
	) {
		IngredientGridBackgroundStyle style = this.backgroundStyle.get();
		if (!style.isEnabled()) {
			return;
		}
		for (Panel panel : panels) {
			if (panel.backgroundArea().isEmpty()) {
				continue;
			}
			if (style == IngredientGridBackgroundStyle.BORDER_ONLY) {
				drawBorder(guiGraphics, panel.backgroundArea());
				continue;
			}
			this.background.draw(guiGraphics, panel.backgroundArea());
			this.slotBackground.draw(guiGraphics, panel.slotBackgroundArea());
			GuiExclusionAreaShadow.draw(
				guiGraphics,
				this.exclusionAreaShadow,
				panel.backgroundArea(),
				guiExclusionAreas
			);
		}
	}

	private void drawBorder(GuiGraphicsExtractor guiGraphics, ImmutableRect2i area) {
		int padding = IngredientGridWithNavigationLayout.BORDER_PADDING;
		ImmutableRect2i sides = area.cropTop(padding).cropBottom(padding);
		drawBorderSection(guiGraphics, area, area.keepTop(padding));
		drawBorderSection(guiGraphics, area, area.keepBottom(padding));
		drawBorderSection(guiGraphics, area, sides.keepLeft(padding));
		drawBorderSection(guiGraphics, area, sides.keepRight(padding));
	}

	private void drawBorderSection(GuiGraphicsExtractor guiGraphics, ImmutableRect2i area, ImmutableRect2i section) {
		if (section.isEmpty()) {
			return;
		}
		guiGraphics.enableScissor(section.x(), section.y(), section.x() + section.width(), section.y() + section.height());
		try {
			this.background.draw(guiGraphics, area);
		} finally {
			guiGraphics.disableScissor();
		}
	}

	public record Panel(ImmutableRect2i backgroundArea, ImmutableRect2i slotBackgroundArea) {

	}
}

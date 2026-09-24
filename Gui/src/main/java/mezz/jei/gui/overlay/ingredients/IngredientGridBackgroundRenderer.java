package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.Set;

/**
 * Draws ingredient-grid panel backgrounds, slot backgrounds, and exclusion-area shadows.
 */
public final class IngredientGridBackgroundRenderer {
	private final ScalableDrawable background;
	private final ScalableDrawable slotBackground;
	private final ScalableDrawable exclusionAreaShadow;

	public IngredientGridBackgroundRenderer(
		ScalableDrawable background,
		ScalableDrawable slotBackground,
		ScalableDrawable exclusionAreaShadow
	) {
		this.background = background;
		this.slotBackground = slotBackground;
		this.exclusionAreaShadow = exclusionAreaShadow;
	}

	public void draw(
		GuiGraphicsExtractor guiGraphics,
		List<Panel> panels,
		Set<ImmutableRect2i> guiExclusionAreas
	) {
		ImmutableRect2i backgroundArea = panels.stream()
			.map(Panel::backgroundArea)
			.reduce(ImmutableRect2i.EMPTY, MathUtil::union);
		if (backgroundArea.isEmpty()) {
			return;
		}
		this.background.draw(guiGraphics, backgroundArea);
		for (Panel panel : panels) {
			this.slotBackground.draw(guiGraphics, panel.slotBackgroundArea());
		}
		GuiExclusionAreaShadow.draw(
			guiGraphics,
			this.exclusionAreaShadow,
			backgroundArea,
			guiExclusionAreas
		);
	}

	public record Panel(ImmutableRect2i backgroundArea, ImmutableRect2i slotBackgroundArea) {

	}
}

package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Set;

/**
 * Draws ingredient-grid panel backgrounds, slot backgrounds, and exclusion-area shadows.
 */
public final class IngredientGridBackgroundRenderer {
	private final DrawableNineSliceTexture background;
	private final DrawableNineSliceTexture slotBackground;
	private final DrawableNineSliceTexture exclusionAreaShadow;

	public IngredientGridBackgroundRenderer(
		DrawableNineSliceTexture background,
		DrawableNineSliceTexture slotBackground,
		DrawableNineSliceTexture exclusionAreaShadow
	) {
		this.background = background;
		this.slotBackground = slotBackground;
		this.exclusionAreaShadow = exclusionAreaShadow;
	}

	public void draw(
		GuiGraphics guiGraphics,
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

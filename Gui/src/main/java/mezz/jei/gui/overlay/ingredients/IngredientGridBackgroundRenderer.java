package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.util.ImmutableRect2i;
import com.mojang.blaze3d.vertex.PoseStack;

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
		PoseStack poseStack,
		List<Panel> panels,
		Set<ImmutableRect2i> guiExclusionAreas
	) {
		for (Panel panel : panels) {
			if (panel.backgroundArea().isEmpty()) {
				continue;
			}
			this.background.draw(poseStack, panel.backgroundArea());
			this.slotBackground.draw(poseStack, panel.slotBackgroundArea());
			GuiExclusionAreaShadow.draw(
				poseStack,
				this.exclusionAreaShadow,
				panel.backgroundArea(),
				guiExclusionAreas
			);
		}
	}

	public record Panel(ImmutableRect2i backgroundArea, ImmutableRect2i slotBackgroundArea) {

	}
}

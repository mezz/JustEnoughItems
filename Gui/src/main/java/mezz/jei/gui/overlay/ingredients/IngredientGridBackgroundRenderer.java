package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IngredientGridBackgroundStyle;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.util.ImmutableRect2i;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mezzdev.config.api.value.IConfigValue;

import java.util.List;
import java.util.Set;

/**
 * Draws ingredient-grid panel backgrounds, slot backgrounds, and exclusion-area shadows.
 */
public final class IngredientGridBackgroundRenderer {
	private final IConfigValue<IngredientGridBackgroundStyle> backgroundStyle;
	private final DrawableNineSliceTexture background;
	private final DrawableNineSliceTexture slotBackground;
	private final DrawableNineSliceTexture exclusionAreaShadow;

	public IngredientGridBackgroundRenderer(
		IConfigValue<IngredientGridBackgroundStyle> backgroundStyle,
		DrawableNineSliceTexture background,
		DrawableNineSliceTexture slotBackground,
		DrawableNineSliceTexture exclusionAreaShadow
	) {
		this.backgroundStyle = backgroundStyle;
		this.background = background;
		this.slotBackground = slotBackground;
		this.exclusionAreaShadow = exclusionAreaShadow;
	}

	public void draw(
		PoseStack poseStack,
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
				drawBorder(poseStack, panel.backgroundArea());
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

	private void drawBorder(PoseStack poseStack, ImmutableRect2i area) {
		int padding = IngredientGridWithNavigationLayout.BORDER_PADDING;
		ImmutableRect2i sides = area.cropTop(padding).cropBottom(padding);
		drawBorderSection(poseStack, area, area.keepTop(padding));
		drawBorderSection(poseStack, area, area.keepBottom(padding));
		drawBorderSection(poseStack, area, sides.keepLeft(padding));
		drawBorderSection(poseStack, area, sides.keepRight(padding));
	}

	private void drawBorderSection(PoseStack poseStack, ImmutableRect2i area, ImmutableRect2i section) {
		if (section.isEmpty()) {
			return;
		}
		mezz.jei.common.gui.ScissorHelper.enableScissor(poseStack, section.x(), section.y(), section.x() + section.width(), section.y() + section.height());
		try {
			this.background.draw(poseStack, area);
		} finally {
			mezz.jei.common.gui.ScissorHelper.disableScissor();
		}
	}

	public record Panel(ImmutableRect2i backgroundArea, ImmutableRect2i slotBackgroundArea) {

	}
}

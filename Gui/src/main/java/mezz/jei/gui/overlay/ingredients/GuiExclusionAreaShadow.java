package mezz.jei.gui.overlay.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;

import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.util.ImmutableRect2i;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GuiExclusionAreaShadow {
	static final int SHADOW_SIZE = 4;

	private GuiExclusionAreaShadow() {

	}

	public static void draw(
		PoseStack poseStack,
		DrawableNineSliceTexture shadow,
		ImmutableRect2i backgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas
	) {
		List<ImmutableRect2i> shadowAreas = calculateShadowAreas(backgroundArea, guiExclusionAreas);
		if (shadowAreas.isEmpty()) {
			return;
		}

		mezz.jei.common.gui.ScissorHelper.enableScissor(poseStack,
			backgroundArea.x(),
			backgroundArea.y(),
			backgroundArea.x() + backgroundArea.width(),
			backgroundArea.y() + backgroundArea.height()
		);
		try {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			for (ImmutableRect2i shadowArea : shadowAreas) {
				shadow.draw(poseStack, shadowArea);
			}
		} finally {
			RenderSystem.disableBlend();
			mezz.jei.common.gui.ScissorHelper.disableScissor();
		}
	}

	static List<ImmutableRect2i> calculateShadowAreas(
		ImmutableRect2i backgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas
	) {
		if (backgroundArea.isEmpty() || guiExclusionAreas.isEmpty()) {
			return List.of();
		}

		List<ImmutableRect2i> shadowAreas = new ArrayList<>();
		for (ImmutableRect2i exclusionArea : guiExclusionAreas) {
			if (!exclusionArea.isEmpty() && exclusionArea.intersects(backgroundArea)) {
				shadowAreas.add(new ImmutableRect2i(
					exclusionArea.x() - SHADOW_SIZE,
					exclusionArea.y() - SHADOW_SIZE,
					exclusionArea.width() + 2 * SHADOW_SIZE,
					exclusionArea.height() + 2 * SHADOW_SIZE
				));
			}
		}

		return List.copyOf(shadowAreas);
	}
}

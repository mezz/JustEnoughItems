package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class GuiExclusionAreaShadow {
	private static final int FILL_ALPHA = 0x20;
	private static final int[] SHADOW_ALPHAS = {
		0x40,
		0x2C,
		0x1D,
		0x12,
		0x0A,
		0x05
	};
	static final int SHADOW_SIZE = SHADOW_ALPHAS.length;

	private GuiExclusionAreaShadow() {

	}

	public static void draw(
		GuiGraphicsExtractor guiGraphics,
		ImmutableRect2i backgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas
	) {
		for (ShadowBand band : calculateShadowBands(backgroundArea, guiExclusionAreas)) {
			ImmutableRect2i area = band.area();
			guiGraphics.fill(
				area.x(),
				area.y(),
				area.x() + area.width(),
				area.y() + area.height(),
				band.color()
			);
		}
	}

	static List<ShadowBand> calculateShadowBands(
		ImmutableRect2i backgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas
	) {
		if (backgroundArea.isEmpty() || guiExclusionAreas.isEmpty()) {
			return List.of();
		}

		List<ShadowBand> bands = new ArrayList<>();
		for (ImmutableRect2i exclusionArea : guiExclusionAreas) {
			if (!exclusionArea.isEmpty() && exclusionArea.intersects(backgroundArea)) {
				addShadowBands(bands, backgroundArea, exclusionArea);
			}
		}
		return List.copyOf(bands);
	}

	private static void addShadowBands(
		List<ShadowBand> bands,
		ImmutableRect2i clipArea,
		ImmutableRect2i exclusionArea
	) {
		addClippedBand(
			bands,
			clipArea,
			exclusionArea.x(),
			exclusionArea.y(),
			exclusionArea.width(),
			exclusionArea.height(),
			FILL_ALPHA << 24
		);

		for (int i = 0; i < SHADOW_SIZE; i++) {
			int distance = i + 1;
			int color = SHADOW_ALPHAS[i] << 24;
			int outerX = exclusionArea.x() - distance;
			int outerY = exclusionArea.y() - distance;
			int outerRight = exclusionArea.x() + exclusionArea.width() + distance;
			int outerBottom = exclusionArea.y() + exclusionArea.height() + distance;
			int outerWidth = outerRight - outerX;
			int outerHeight = outerBottom - outerY;

			addClippedBand(bands, clipArea, outerX, outerY, outerWidth, 1, color);
			addClippedBand(bands, clipArea, outerX, outerBottom - 1, outerWidth, 1, color);
			addClippedBand(bands, clipArea, outerX, outerY + 1, 1, outerHeight - 2, color);
			addClippedBand(bands, clipArea, outerRight - 1, outerY + 1, 1, outerHeight - 2, color);
		}
	}

	private static void addClippedBand(
		List<ShadowBand> bands,
		ImmutableRect2i clipArea,
		int x,
		int y,
		int width,
		int height,
		int color
	) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int clippedX = Math.max(x, clipArea.x());
		int clippedY = Math.max(y, clipArea.y());
		int clippedRight = Math.min(x + width, clipArea.x() + clipArea.width());
		int clippedBottom = Math.min(y + height, clipArea.y() + clipArea.height());
		if (clippedRight <= clippedX || clippedBottom <= clippedY) {
			return;
		}

		bands.add(new ShadowBand(
			new ImmutableRect2i(
				clippedX,
				clippedY,
				clippedRight - clippedX,
				clippedBottom - clippedY
			),
			color
		));
	}

	record ShadowBand(ImmutableRect2i area, int color) {

	}
}

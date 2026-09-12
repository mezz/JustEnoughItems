package mezz.jei.gui.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;

enum Alignment {
	TOP_LEFT(HorizontalAlignment.LEFT, VerticalAlignment.TOP),
	TOP_CENTER(HorizontalAlignment.CENTER, VerticalAlignment.TOP),
	TOP_RIGHT(HorizontalAlignment.RIGHT, VerticalAlignment.TOP),
	CENTER_LEFT(HorizontalAlignment.LEFT, VerticalAlignment.CENTER),
	CENTER(HorizontalAlignment.CENTER, VerticalAlignment.CENTER),
	CENTER_RIGHT(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER),
	BOTTOM_LEFT(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM),
	BOTTOM_CENTER(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM),
	BOTTOM_RIGHT(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);

	private final HorizontalAlignment horizontalAlignment;
	private final VerticalAlignment verticalAlignment;

	Alignment(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
	}

	public HorizontalAlignment horizontalAlignment() {
		return horizontalAlignment;
	}

	public VerticalAlignment verticalAlignment() {
		return verticalAlignment;
	}

	public static Alignment from(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		for (Alignment alignment : values()) {
			if (alignment.horizontalAlignment == horizontalAlignment && alignment.verticalAlignment == verticalAlignment) {
				return alignment;
			}
		}
		return CENTER;
	}
}

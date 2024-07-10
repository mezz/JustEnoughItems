package mezz.jei.common.config;

import java.util.List;

public enum BookmarkTooltipFeature {
	PREVIEW,
	INGREDIENTS;

	public static final List<BookmarkTooltipFeature> DEFAULT_BOOKMARK_TOOLTIP_FEATURES = List.of(PREVIEW, INGREDIENTS);
}

package mezz.jei.common.config;

import java.util.List;

public enum BookmarkFeature {
	PREVIEW,
	INGREDIENTS;

	public static final List<BookmarkFeature> defaultBookmarkFeatures = List.of(PREVIEW, INGREDIENTS);
}

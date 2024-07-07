package mezz.jei.common.config;

import java.util.List;

public enum BookmarkFeature {
    PREVIEW(false),
    INGREDIENTS(true);

    private final boolean withShift;

    BookmarkFeature(boolean withShift) {

        this.withShift = withShift;
    }

    public static final List<BookmarkFeature> defaultBookmarkFeatures = List.of(PREVIEW,INGREDIENTS);

    public boolean withShift() {
        return withShift;
    }
}

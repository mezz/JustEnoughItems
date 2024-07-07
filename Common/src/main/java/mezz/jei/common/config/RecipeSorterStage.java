package mezz.jei.common.config;

import java.util.List;

public enum RecipeSorterStage {
	BOOKMARKED, CRAFTABLE;

	public static final List<RecipeSorterStage> defaultStages = List.of(
		RecipeSorterStage.BOOKMARKED,
		RecipeSorterStage.CRAFTABLE
	);
}

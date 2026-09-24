package mezz.jei.common.config;

import java.util.EnumSet;
import java.util.Set;

public enum RecipeSorterStage {
	BOOKMARKED, CRAFTABLE;

	public boolean isEnabled(IClientConfig clientConfig) {
		return switch (this) {
			case BOOKMARKED -> clientConfig.recipeSortingBookmarksEnabled().get();
			case CRAFTABLE -> clientConfig.recipeSortingCraftableEnabled().get();
		};
	}

	public void setEnabled(IClientConfig clientConfig, boolean enabled) {
		switch (this) {
			case BOOKMARKED -> clientConfig.recipeSortingBookmarksEnabled().set(enabled);
			case CRAFTABLE -> clientConfig.recipeSortingCraftableEnabled().set(enabled);
		}
	}

	public static Set<RecipeSorterStage> getEnabled(IClientConfig clientConfig) {
		Set<RecipeSorterStage> stages = EnumSet.noneOf(RecipeSorterStage.class);
		for (RecipeSorterStage stage : values()) {
			if (stage.isEnabled(clientConfig)) {
				stages.add(stage);
			}
		}
		return Set.copyOf(stages);
	}
}

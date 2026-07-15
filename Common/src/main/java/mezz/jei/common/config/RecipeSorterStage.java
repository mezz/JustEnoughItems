package mezz.jei.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum RecipeSorterStage {
	BOOKMARKED, CRAFTABLE;

	public static final List<RecipeSorterStage> defaultStages = List.of(
		RecipeSorterStage.BOOKMARKED,
		RecipeSorterStage.CRAFTABLE
	);

	public boolean isEnabled(IClientConfig clientConfig) {
		return clientConfig.recipeSorterStages().getValue().contains(this);
	}

	public void setEnabled(IClientConfig clientConfig, boolean enabled) {
		List<RecipeSorterStage> recipeSorterStages = clientConfig.recipeSorterStages().getValue();
		boolean currentlyEnabled = recipeSorterStages.contains(this);
		if (enabled == currentlyEnabled) {
			return;
		}

		recipeSorterStages = new ArrayList<>(recipeSorterStages);
		if (enabled) {
			recipeSorterStages.add(this);
		} else {
			recipeSorterStages.remove(this);
		}
		clientConfig.recipeSorterStages().set(recipeSorterStages);
	}

	public static Set<RecipeSorterStage> getEnabled(IClientConfig clientConfig) {
		return Set.copyOf(clientConfig.recipeSorterStages().getValue());
	}
}

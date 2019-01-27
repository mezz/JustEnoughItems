package mezz.jei.plugins.vanilla.anvil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class AnvilRecipeDataCache {
	private static final LoadingCache<AnvilRecipeWrapper, AnvilRecipeDisplayData> cachedDisplayData = CacheBuilder.newBuilder()
		.maximumSize(25)
		.build(new CacheLoader<AnvilRecipeWrapper, AnvilRecipeDisplayData>() {
			@Override
			public AnvilRecipeDisplayData load(AnvilRecipeWrapper key) {
				return new AnvilRecipeDisplayData();
			}
		});

	public static AnvilRecipeDisplayData getDisplayData(AnvilRecipeWrapper recipeWrapper) {
		return cachedDisplayData.getUnchecked(recipeWrapper);
	}
}

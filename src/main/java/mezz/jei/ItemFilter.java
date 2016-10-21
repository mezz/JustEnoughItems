package mezz.jei;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;

public class ItemFilter {

	/**
	 * A cache for fast searches while typing or using backspace. Maps filterText to filteredItemMaps
	 */
	private final LoadingCache<String, ImmutableList<IIngredientListElement>> filteredItemMapsCache = CacheBuilder.newBuilder()
			.maximumWeight(16)
			.weigher(new OneWeigher())
			.concurrencyLevel(1)
			.build(new ItemFilterCacheLoader());

	private ImmutableList<IIngredientListElement> baseList;

	public void build() {
		Log.info("Building item filter...");
		long start_time = System.currentTimeMillis();

		this.baseList = IngredientBaseListFactory.create();
		this.filteredItemMapsCache.invalidateAll();

		Log.info("Built    item filter in {} ms", System.currentTimeMillis() - start_time);
	}

	public ImmutableList<IIngredientListElement> getIngredientList() {
		String[] filters = Config.getFilterText().split("\\|");

		if (filters.length == 1) {
			String filter = filters[0];
			return filteredItemMapsCache.getUnchecked(filter);
		} else {
			ImmutableList.Builder<IIngredientListElement> ingredientList = ImmutableList.builder();
			for (String filter : filters) {
				List<IIngredientListElement> ingredients = filteredItemMapsCache.getUnchecked(filter);
				ingredientList.addAll(ingredients);
			}
			return ingredientList.build();
		}
	}

	/**
	 * {@link #getItemStacks()} is slow, so cache the previous value in case someone requests it often.
	 */
	private ImmutableList<ItemStack> itemStacksCached = ImmutableList.of();
	private String filterCached;

	public ImmutableList<ItemStack> getItemStacks() {
		if (!Config.getFilterText().equals(filterCached)) {
			ImmutableList.Builder<ItemStack> filteredStacks = ImmutableList.builder();
			for (IIngredientListElement element : getIngredientList()) {
				Object ingredient = element.getIngredient();
				if (ingredient instanceof ItemStack) {
					filteredStacks.add((ItemStack) ingredient);
				}
			}
			itemStacksCached = filteredStacks.build();
			filterCached = Config.getFilterText();
		}
		return itemStacksCached;
	}

	public int size() {
		return getIngredientList().size();
	}

	private static class OneWeigher implements Weigher<String, ImmutableList<IIngredientListElement>> {
		public int weigh(String key, ImmutableList<IIngredientListElement> value) {
			return 1;
		}
	}

	private class ItemFilterCacheLoader extends CacheLoader<String, ImmutableList<IIngredientListElement>> {
		@Override
		public ImmutableList<IIngredientListElement> load(final String filterText) throws Exception {
			if (filterText.length() == 0) {
				return baseList;
			}

			// Recursive.
			// Find a cached filter that is before the one we want, so we don't have to filter the full item list.
			// For example, the "", "i", "ir", and "iro" filters contain everything in the "iron" filter and more.
			String prevFilterText = filterText.substring(0, filterText.length() - 1);

			ImmutableList<IIngredientListElement> baseItemSet = filteredItemMapsCache.get(prevFilterText);

			FilterPredicate filterPredicate = new FilterPredicate(filterText);

			ImmutableList.Builder<IIngredientListElement> builder = ImmutableList.builder();
			for (IIngredientListElement itemStackElement : baseItemSet) {
				if (filterPredicate.apply(itemStackElement)) {
					builder.add(itemStackElement);
				}
			}
			return builder.build();
		}
	}


	private static class FilterPredicate implements Predicate<IIngredientListElement> {
		private final List<String> searchTokens = new ArrayList<String>();
		private final List<String> modNameTokens = new ArrayList<String>();
		private final List<String> tooltipTokens = new ArrayList<String>();
		private final List<String> oreDictTokens = new ArrayList<String>();
		private final List<String> creativeTabTokens = new ArrayList<String>();
		private final List<String> colorTokens = new ArrayList<String>();

		public FilterPredicate(String filterText) {
			String[] tokens = filterText.split(" ");
			for (String token : tokens) {
				if (token.isEmpty()) {
					continue;
				}

				if (token.startsWith("@")) {
					addTokenWithoutPrefix(token, modNameTokens);
				} else if (token.startsWith("#")) {
					addTokenWithoutPrefix(token, tooltipTokens);
				} else if (token.startsWith("$")) {
					addTokenWithoutPrefix(token, oreDictTokens);
				} else if (token.startsWith("%")) {
					addTokenWithoutPrefix(token, creativeTabTokens);
				} else if (token.startsWith("^")) {
					addTokenWithoutPrefix(token, colorTokens);
				} else {
					searchTokens.add(token);
				}
			}
		}

		private static void addTokenWithoutPrefix(String token, List<String> tokensList) {
			if (token.length() < 2) {
				return;
			}
			String tokenText = token.substring(1);
			tokensList.add(tokenText);
		}

		@Override
		public boolean apply(@Nullable IIngredientListElement input) {
			if (input == null) {
				return false;
			}

			if (!stringContainsTokens(input.getModNameString(), modNameTokens)) {
				return false;
			}

			if (!stringContainsTokens(input.getTooltipString(), tooltipTokens)) {
				return false;
			}

			if (!stringContainsTokens(input.getOreDictString(), oreDictTokens)) {
				return false;
			}

			if (!stringContainsTokens(input.getCreativeTabsString(), creativeTabTokens)) {
				return false;
			}

			if (!stringContainsTokens(input.getColorString(), colorTokens)) {
				return false;
			}

			return stringContainsTokens(input.getSearchString(), searchTokens);
		}

		private static boolean stringContainsTokens(String comparisonString, List<String> tokens) {
			for (String token : tokens) {
				if (!comparisonString.contains(token)) {
					return false;
				}
			}
			return true;
		}
	}
}

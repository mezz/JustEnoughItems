package mezz.jei;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.IItemRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.ItemStackElement;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemFilter {

	/** A cache for fast searches while typing or using backspace. Maps filterText to filteredItemMaps */
	private final LoadingCache<String, ImmutableList<ItemStackElement>> filteredItemMapsCache;

	public ItemFilter(final IItemRegistry itemRegistry) {
		filteredItemMapsCache = CacheBuilder.newBuilder()
				.maximumWeight(16)
				.weigher(new SearchFilterWeigher())
				.concurrencyLevel(1)
				.build(new ItemFilterCacheLoader(itemRegistry));

		// preload the base list
		filteredItemMapsCache.getUnchecked("");
	}

	public void reset() {
		this.filteredItemMapsCache.invalidateAll();
	}

	@Nonnull
	public String getFilterText() {
		return Config.getFilterText();
	}

	@Nonnull
	public ImmutableList<ItemStackElement> getItemList() {
		String[] filters = getFilterText().split("\\|");

		if (filters.length == 1) {
			String filter = filters[0];
			return filteredItemMapsCache.getUnchecked(filter);
		} else {
			ImmutableList.Builder<ItemStackElement> itemList = ImmutableList.builder();
			for (String filter : filters) {
				List<ItemStackElement> itemStackElements = filteredItemMapsCache.getUnchecked(filter);
				itemList.addAll(itemStackElements);
			}
			return itemList.build();
		}
	}

	public int size() {
		return getItemList().size();
	}

	private static ImmutableList<ItemStackElement> createBaseList(IItemRegistry itemRegistry) {
		ItemStackChecker itemStackChecker = new ItemStackChecker();

		ImmutableList.Builder<ItemStackElement> baseList = ImmutableList.builder();
		for (ItemStack itemStack : itemRegistry.getItemList()) {
			if (itemStack == null) {
				continue;
			}

			if (itemStackChecker.isItemStackHidden(itemStack)) {
				continue;
			}

			ItemStackElement itemStackElement = ItemStackElement.create(itemStack);
			if (itemStackElement != null) {
				baseList.add(itemStackElement);
			}
		}

		for (Multiset.Entry<Item> brokenItem : itemStackChecker.getBrokenItems().entrySet()) {
			int count = brokenItem.getCount();
			if (count > 1) {
				Item item = brokenItem.getElement();
				String modName = Internal.getItemRegistry().getModNameForItem(item);
				Log.error("Couldn't get ItemModel for {} item {}. Suppressed {} similar errors.", modName, item, count);
			}
		}

		return baseList.build();
	}

	private static class SearchFilterWeigher implements Weigher<String, ImmutableList<ItemStackElement>> {
		public int weigh(@Nonnull String key, @Nonnull ImmutableList<ItemStackElement> value) {
			// The CacheLoader is recursive, so keep the base value in the cache permanently by setting its weight to 0
			return (key.length() == 0) ? 0 : 1;
		}
	}

	private class ItemFilterCacheLoader extends CacheLoader<String, ImmutableList<ItemStackElement>> {
		private final IItemRegistry itemRegistry;

		public ItemFilterCacheLoader(IItemRegistry itemRegistry) {
			this.itemRegistry = itemRegistry;
		}

		@Override
		public ImmutableList<ItemStackElement> load(@Nonnull final String filterText) throws Exception {
			if (filterText.length() == 0) {
				return createBaseList(itemRegistry);
			}

			// Recursive.
			// Find a cached filter that is before the one we want, so we don't have to filter the full item list.
			// For example, the "", "i", "ir", and "iro" filters contain everything in the "iron" filter and more.
			String prevFilterText = filterText.substring(0, filterText.length() - 1);

			ImmutableList<ItemStackElement> baseItemSet = filteredItemMapsCache.get(prevFilterText);

			FilterPredicate filterPredicate = new FilterPredicate(filterText);

			ImmutableList.Builder<ItemStackElement> itemStackElementsBuilder = ImmutableList.builder();
			for (ItemStackElement itemStackElement : baseItemSet) {
				if (filterPredicate.apply(itemStackElement)) {
					itemStackElementsBuilder.add(itemStackElement);
				}
			}
			return itemStackElementsBuilder.build();
		}
	}

	private static class ItemStackChecker {
		private final IItemBlacklist itemBlacklist;
		private final ItemModelMesher itemModelMesher;
		private final IBakedModel missingModel;
		private final Multiset<Item> brokenItems = HashMultiset.create();

		public ItemStackChecker() {
			itemBlacklist = Internal.getHelpers().getItemBlacklist();
			itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
			missingModel = itemModelMesher.getModelManager().getMissingModel();
		}

		public boolean isItemStackHidden(@Nonnull ItemStack itemStack) {
			if (isItemHiddenByBlacklist(itemStack)) {
				return true;
			}

			return isItemStackHiddenByMissingModel(itemStack);
		}

		public Multiset<Item> getBrokenItems() {
			return brokenItems;
		}

		private boolean isItemStackHiddenByMissingModel(@Nonnull ItemStack itemStack) {
			Item item = itemStack.getItem();
			if (brokenItems.contains(item)) {
				return true;
			}

			final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
			final IBakedModel itemModel;
			try {
				itemModel = renderItem.getItemModelWithOverrides(itemStack, null, null);
			} catch (RuntimeException | LinkageError e) {
				String modName = Internal.getItemRegistry().getModNameForItem(item);
				String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
				Log.error("Couldn't get ItemModel for {} itemStack {}", modName, stackInfo, e);
				brokenItems.add(item);
				return true;
			}

			if (Config.isHideMissingModelsEnabled()) {
				return itemModel == null || itemModel == missingModel;
			}

			return false;
		}

		private boolean isItemHiddenByBlacklist(@Nonnull ItemStack itemStack) {
			try {
				if (!itemBlacklist.isItemBlacklisted(itemStack)) {
					return false;
				}
			} catch (RuntimeException e) {
				String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
				Log.error("Could not check blacklist for stack {}", stackInfo, e);
				return true;
			}

			if (Config.isEditModeEnabled()) {
				// edit mode can only change the config blacklist, not things blacklisted through the API
				return !Config.isItemOnConfigBlacklist(itemStack);
			}

			return true;
		}
	}

	private static class FilterPredicate implements Predicate<ItemStackElement> {
		private final List<String> searchTokens = new ArrayList<>();
		private final List<String> modNameTokens = new ArrayList<>();
		private final List<String> tooltipTokens = new ArrayList<>();
		private final List<String> oreDictTokens = new ArrayList<>();
		private final List<String> creativeTabTokens = new ArrayList<>();
		private final List<String> colorTokens = new ArrayList<>();

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
		public boolean apply(@Nullable ItemStackElement input) {
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

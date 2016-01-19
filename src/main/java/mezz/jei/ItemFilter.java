package mezz.jei;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.IItemRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.ItemStackElement;
import mezz.jei.util.Log;

public class ItemFilter {
	/** The currently active filter text */
	@Nonnull
	private static String filterText = "";

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

	public static boolean setFilterText(@Nonnull String filterText) {
		String lowercaseFilterText = filterText.toLowerCase();
		if (ItemFilter.filterText.equals(lowercaseFilterText)) {
			return false;
		}

		ItemFilter.filterText = lowercaseFilterText;
		return true;
	}

	@Nonnull
	public String getFilterText() {
		return filterText;
	}

	@Nonnull
	public ImmutableList<ItemStackElement> getItemList() {
		String[] filters = filterText.split("\\|");

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
		@SuppressWarnings("deprecation")
		private final IBakedModel missingModel;
		private final Multiset<Item> brokenItems = HashMultiset.create();

		public ItemStackChecker() {
			itemBlacklist = Internal.getHelpers().getItemBlacklist();
			itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
			missingModel = itemModelMesher.getModelManager().getMissingModel();
		}

		public boolean isItemStackHidden(@Nonnull ItemStack itemStack) {
			if (isItemStackHiddenByMissingModel(itemStack)) {
				return true;
			}

			return isItemHiddenByBlacklist(itemStack);
		}

		public Multiset<Item> getBrokenItems() {
			return brokenItems;
		}

		private boolean isItemStackHiddenByMissingModel(@Nonnull ItemStack itemStack) {
			try {
				if (itemModelMesher.getItemModel(itemStack) == missingModel && Config.isHideMissingModelsEnabled()) {
					return true;
				}
			} catch (RuntimeException | AbstractMethodError e) {
				Item item = itemStack.getItem();
				if (!brokenItems.contains(item)) {
					String modName = Internal.getItemRegistry().getModNameForItem(item);
					Log.error("Couldn't get ItemModel for {} itemStack {}.", modName, itemStack, e);
				}
				brokenItems.add(item);
				return true;
			}
			return false;
		}

		private boolean isItemHiddenByBlacklist(@Nonnull ItemStack itemStack) {
			if (!itemBlacklist.isItemBlacklisted(itemStack)) {
				return false;
			}

			if (Config.isEditModeEnabled()) {
				// edit mode can only change the config blacklist, not things blacklisted through the API
				return !Config.isItemOnConfigBlacklist(itemStack, true) && !Config.isItemOnConfigBlacklist(itemStack, false);
			}

			return true;
		}
	}

	private static class FilterPredicate implements Predicate<ItemStackElement> {
		private final List<String> itemNameTokens = new ArrayList<>();
		private final List<String> modNameTokens = new ArrayList<>();

		public FilterPredicate(String filterText) {
			String[] tokens = filterText.split(" ");
			for (String token : tokens) {
				if (token.startsWith("@")) {
					String modNameToken = token.substring(1);
					modNameTokens.add(modNameToken);
				} else {
					itemNameTokens.add(token);
				}
			}
		}

		@Override
		public boolean apply(@Nullable ItemStackElement input) {
			if (input == null) {
				return false;
			}

			String modName = input.getModName();
			for (String token : modNameTokens) {
				if (!modName.contains(token)) {
					return false;
				}
			}

			String itemName = input.getSearchString();
			for (String token : itemNameTokens) {
				if (!itemName.contains(token)) {
					return false;
				}
			}

			return true;
		}
	}
}

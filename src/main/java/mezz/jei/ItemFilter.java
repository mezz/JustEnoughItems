package mezz.jei;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.IBakedModel;
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
				.build(new ItemFilterCacheLoader(itemRegistry));
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
		return filteredItemMapsCache.getUnchecked(filterText);
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
			Collection<ItemStackElement> filteredItemList = Collections2.filter(baseItemSet, filterPredicate);

			return ImmutableList.copyOf(filteredItemList);
		}
	}

	private static class ItemStackChecker {
		private IItemBlacklist itemBlacklist;
		private ItemModelMesher itemModelMesher;
		@SuppressWarnings("deprecation")
		private IBakedModel missingModel;

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

		private boolean isItemStackHiddenByMissingModel(@Nonnull ItemStack itemStack) {
			if (!Config.isHideMissingModelsEnabled()) {
				return false;
			}

			try {
				if (itemModelMesher.getItemModel(itemStack) == missingModel) {
					return true;
				}
			} catch (Exception e) {
				Log.error("Couldn't get ItemModel for itemStack {}.", itemStack.getClass(), e);
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
		private final String filterText;

		public FilterPredicate(String filterText) {
			this.filterText = filterText;
		}

		@Override
		public boolean apply(@Nullable ItemStackElement input) {
			if (input == null) {
				return false;
			}

			String[] tokens = filterText.split(" ");
			for (String token : tokens) {
				if (token.startsWith("@")) {
					String modNameFilter = token.substring(1);
					if (modNameFilter.length() > 0 && !input.getModName().contains(modNameFilter)) {
						return false;
					}
				} else {
					if (!input.getLocalizedName().contains(token)) {
						return false;
					}
				}
			}
			return true;
		}
	}
}

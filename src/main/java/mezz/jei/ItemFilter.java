package mezz.jei;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import mezz.jei.util.ItemStackElement;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class ItemFilter {
	/** The currently active filter text */
	@Nonnull
	private String filterText = "";

	/** A cache for fast searches while typing or using backspace. Maps filterText to filteredItemMaps */
	private final LoadingCache<String, ImmutableList<ItemStackElement>> filteredItemMapsCache;

	public ItemFilter(@Nonnull List<ItemStack> itemStacks) {

		filteredItemMapsCache = CacheBuilder.newBuilder()
			.maximumWeight(16)
			.weigher(new Weigher<String, ImmutableList<ItemStackElement>>() {
				public int weigh(@Nonnull String key, @Nonnull ImmutableList<ItemStackElement> value) {
					// The CacheLoader is recursive, so keep the base value in the cache permanently by setting its weight to 0
					return (key.length() == 0) ? 0 : 1;
				}
			})
			.build(new CacheLoader<String, ImmutableList<ItemStackElement>>() {
				@Override
				public ImmutableList<ItemStackElement> load(@Nonnull final String filterText) throws Exception {
					// Recursive.
					// Find a cached filter that is before the one we want, so we don't have to filter the full item list.
					// For example, the "", "i", "ir", and "iro" filters contain everything in the "iron" filter and more.
					String prevFilterText = filterText.substring(0, filterText.length() - 1);

					ImmutableList<ItemStackElement> baseItemSet = filteredItemMapsCache.get(prevFilterText);

					Collection<ItemStackElement> filteredItemList = Collections2.filter(baseItemSet,
						new Predicate<ItemStackElement>() {
							@Override
							public boolean apply(@Nullable ItemStackElement input) {
								return input != null && input.getLocalizedName().contains(filterText);
							}
						}
					);

					return ImmutableList.copyOf(filteredItemList);
				}
			});

		// create the recursive base case value, the list with no filter set
		ImmutableList.Builder<ItemStackElement> baseList = ImmutableList.builder();
		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null)
				continue;

			ItemStackElement itemStackElement = ItemStackElement.create(itemStack);
			if (itemStackElement != null)
				baseList.add(itemStackElement);
		}

		filteredItemMapsCache.put("", baseList.build());
	}

	public boolean setFilterText(@Nonnull String filterText) {
		filterText = filterText.toLowerCase();
		if (this.filterText.equals(filterText))
			return false;

		this.filterText = filterText;
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

}

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
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.item.ItemStack;

import mezz.jei.api.JEIManager;
import mezz.jei.config.Config;
import mezz.jei.util.ItemStackElement;
import mezz.jei.util.Log;

public class ItemFilter {
	/** The currently active filter text */
	@Nonnull
	private static String filterText = "";

	/** A cache for fast searches while typing or using backspace. Maps filterText to filteredItemMaps */
	private final LoadingCache<String, ImmutableList<ItemStackElement>> filteredItemMapsCache;

	public ItemFilter() {
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
						if (filterText.length() == 0) {
							return createBaseList();
						}

						// Recursive.
						// Find a cached filter that is before the one we want, so we don't have to filter the full item list.
						// For example, the "", "i", "ir", and "iro" filters contain everything in the "iron" filter and more.
						String prevFilterText = filterText.substring(0, filterText.length() - 1);

						ImmutableList<ItemStackElement> baseItemSet = filteredItemMapsCache.get(prevFilterText);

						Collection<ItemStackElement> filteredItemList = Collections2.filter(baseItemSet,
								new Predicate<ItemStackElement>() {
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
						);

						return ImmutableList.copyOf(filteredItemList);
					}

					private ImmutableList<ItemStackElement> createBaseList() {
						ImmutableList.Builder<ItemStackElement> baseList = ImmutableList.builder();

						ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
						ModelManager modelManager = itemModelMesher.getModelManager();

						for (ItemStack itemStack : JEIManager.itemRegistry.getItemList()) {
							if (itemStack == null) {
								continue;
							}

							if (Config.hideMissingModelsEnabled) {
								// skip over itemStacks that can't be rendered
								try {
									if (itemModelMesher.getItemModel(itemStack) == modelManager.getMissingModel()) {
										continue;
									}
								} catch (RuntimeException e) {
									try {
										Log.error("Couldn't find ItemModelMesher for itemStack {}.", itemStack, e);
									} catch (RuntimeException ignored) {
										Log.error("Couldn't find ItemModelMesher for itemStack.", e);
									}
									continue;
								}
							}

							if (JEIManager.itemBlacklist.isItemBlacklisted(itemStack)) {
								if (Config.editModeEnabled) {
									if (!Config.isItemOnConfigBlacklist(itemStack, true) && !Config.isItemOnConfigBlacklist(itemStack, false)) {
										continue; // edit mode can only change the config blacklist, not things blacklisted through the API
									}
								} else {
									continue;
								}
							}

							ItemStackElement itemStackElement = ItemStackElement.create(itemStack);
							if (itemStackElement != null) {
								baseList.add(itemStackElement);
							}
						}

						return baseList.build();
					}
				});
	}

	public void reset() {
		this.filteredItemMapsCache.invalidateAll();
	}

	public boolean setFilterText(@Nonnull String filterText) {
		filterText = filterText.toLowerCase();
		if (ItemFilter.filterText.equals(filterText)) {
			return false;
		}

		ItemFilter.filterText = filterText;
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

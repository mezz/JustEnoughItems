package mezz.jei.library.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.config.IClientConfigs;
import mezz.jei.common.config.legacy.LegacySortingConfigMigrator;
import net.mezzdev.config.api.IConfigRegistration;
import net.mezzdev.config.api.sorting.ISortingConfig;
import net.mezzdev.config.api.sorting.ISortingSnapshot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public class RecipeCategorySortingConfig {
	private static final String CONFIG_FILE_NAME = "recipe-category-sort-order.ini";
	private static final boolean ALLOWS_REMOVING_VALUES = true;

	private final ISortingConfig<String> sortingConfig;

	public static ISortingConfig<String> create(IConfigRegistration registration) {
		return registration.createSortingConfig(
			CONFIG_FILE_NAME,
			getDefaultSortOrder(),
			ALLOWS_REMOVING_VALUES
		);
	}

	public static ISortingConfig<String> create(
		IConfigRegistration registration,
		Path jeiConfigDirectory,
		@Nullable UUID profileId
	) {
		ISortingConfig<String> sortingConfig = create(registration);
		return LegacySortingConfigMigrator.register(sortingConfig, jeiConfigDirectory, profileId, CONFIG_FILE_NAME);
	}

	public RecipeCategorySortingConfig(ISortingConfig<String> sortingConfig) {
		this.sortingConfig = Objects.requireNonNull(sortingConfig);
	}

	public RecipeCategorySortingConfig(IClientConfigs clientConfigs) {
		this(Objects.requireNonNull(clientConfigs).getRecipeCategorySortingConfig());
	}

	public Snapshot getSnapshot(Collection<IRecipeType<?>> recipeTypes) {
		Map<IRecipeType<?>, String> categoryIds = new HashMap<>();
		List<String> values = new ArrayList<>(recipeTypes.size());
		for (IRecipeType<?> recipeType : recipeTypes) {
			String id = getRecipeCategoryString(recipeType);
			categoryIds.put(recipeType, id);
			values.add(id);
		}
		return new Snapshot(sortingConfig.getSnapshot(values), Map.copyOf(categoryIds));
	}

	public Runnable addChangeListener(Runnable listener) {
		return sortingConfig.addChangeListener(listener);
	}

	private static Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftCraftingFirst = Comparator.comparing((String s) -> {
				String vanillaCrafting = RecipeTypes.CRAFTING.getUid().toString();
				return s.equals(vanillaCrafting);
			})
			.reversed();
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.startsWith(ModIds.MINECRAFT_ID)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftCraftingFirst.thenComparing(minecraftFirst).thenComparing(naturalOrder);
	}

	private static String getRecipeCategoryString(IRecipeType<?> recipeType) {
		return recipeType.getUid().toString();
	}

	public static final class Snapshot {
		private final ISortingSnapshot<String> sorting;
		private final Map<IRecipeType<?>, String> categoryIds;
		private final Comparator<IRecipeType<?>> comparator;

		private Snapshot(ISortingSnapshot<String> sorting, Map<IRecipeType<?>, String> categoryIds) {
			this.sorting = sorting;
			this.categoryIds = categoryIds;
			this.comparator = Comparator.comparing(this::getCategoryId, sorting.getComparator());
		}

		public Comparator<IRecipeType<?>> getComparator() {
			return comparator;
		}

		public boolean isVisible(IRecipeType<?> recipeType) {
			return sorting.isVisible(getCategoryId(recipeType));
		}

		private String getCategoryId(IRecipeType<?> recipeType) {
			String id = categoryIds.get(recipeType);
			if (id != null) {
				return id;
			}
			return getRecipeCategoryString(recipeType);
		}
	}
}

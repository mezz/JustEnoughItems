package mezz.jei.library.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.common.config.IClientConfigs;
import mezz.jei.common.config.legacy.LegacySortingConfigMigrator;
import net.mezzdev.config.api.IConfigRegistration;
import net.mezzdev.config.api.sorting.ISortingConfig;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
		UUID profileId
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

	public Comparator<RecipeType<?>> getComparator(Collection<RecipeType<?>> recipeTypes) {
		List<String> values = recipeTypes.stream()
			.map(RecipeCategorySortingConfig::getRecipeCategoryString)
			.toList();
		Comparator<String> comparator = sortingConfig.getComparator(values);
		return Comparator.comparing(RecipeCategorySortingConfig::getRecipeCategoryString, comparator);
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

	private static String getRecipeCategoryString(RecipeType<?> recipeType) {
		return recipeType.getUid().toString();
	}
}

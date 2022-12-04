package mezz.jei.library.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.core.config.sorting.MappedSortingConfig;
import mezz.jei.core.config.sorting.serializers.SortingSerializers;

import java.nio.file.Path;
import java.util.Comparator;

public class RecipeCategorySortingConfig extends MappedSortingConfig<RecipeType<?>, String> {
	public RecipeCategorySortingConfig(Path path) {
		super(path, SortingSerializers.STRING, r -> r.getUid().toString());
	}

	@Override
	protected Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftCraftingFirst = Comparator.comparing((String s) -> {
			String vanillaCrafting = RecipeTypes.CRAFTING.getUid().toString();
			return s.equals(vanillaCrafting);
		}).reversed();
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.startsWith(ModIds.MINECRAFT_ID)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftCraftingFirst.thenComparing(minecraftFirst).thenComparing(naturalOrder);
	}

}

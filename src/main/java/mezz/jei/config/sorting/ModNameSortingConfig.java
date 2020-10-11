package mezz.jei.config.sorting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.ingredients.IIngredientListElementInfo;

import java.io.File;
import java.util.Comparator;

public class ModNameSortingConfig extends MappedStringSortingConfig<IIngredientListElementInfo<?>> {
	public ModNameSortingConfig(File file) {
		super(file, IIngredientListElementInfo::getModNameForSorting);
	}

	@Override
	protected Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.equals(ModIds.MINECRAFT_NAME)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftFirst.thenComparing(naturalOrder);
	}

}

package mezz.jei.config.sorting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.config.sorting.serializers.SortingSerializers;
import mezz.jei.ingredients.IListElementInfo;

import java.io.File;
import java.util.Comparator;

public class ModNameSortingConfig extends MappedSortingConfig<IListElementInfo<?>, String> {
	public ModNameSortingConfig(File file) {
		super(file, SortingSerializers.STRING, IListElementInfo::getModNameForSorting);
	}

	@Override
	protected Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.equals(ModIds.MINECRAFT_NAME)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftFirst.thenComparing(naturalOrder);
	}

}

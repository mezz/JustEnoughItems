package mezz.jei.gui.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.core.config.sorting.MappedSortingConfig;
import mezz.jei.core.config.sorting.serializers.SortingSerializers;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.nio.file.Path;
import java.util.Comparator;

public class ModNameSortingConfig extends MappedSortingConfig<IListElementInfo<?>, String> {
	public ModNameSortingConfig(Path path) {
		super(path, SortingSerializers.STRING, IListElementInfo::getModNameForSorting);
	}

	@Override
	protected Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.equals(ModIds.MINECRAFT_NAME)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftFirst.thenComparing(naturalOrder);
	}

}

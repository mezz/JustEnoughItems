package mezz.jei.config.sorting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IModIdHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import java.io.File;
import java.util.Comparator;
import java.util.stream.Stream;

public class ModNameSortingConfig extends StringSortingConfig {
	private final IModIdHelper modIdHelper;

	public ModNameSortingConfig(File file, IModIdHelper modIdHelper) {
		super(file);
		this.modIdHelper = modIdHelper;
	}

	@Override
	protected Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.equals(ModIds.MINECRAFT_NAME)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftFirst.thenComparing(naturalOrder);
	}

	@Override
	protected Stream<String> generate() {
		return ModList.get()
			.applyForEachModContainer(ModContainer::getModId)
			.map(modIdHelper::getModNameForModId);
	}
}

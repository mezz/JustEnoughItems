package mezz.jei.config.sorting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.config.sorting.serializers.SortingSerializers;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.util.Comparator;

public class RecipeCategorySortingConfig extends MappedSortingConfig<ResourceLocation, String> {
	public RecipeCategorySortingConfig(File file) {
		super(file, SortingSerializers.STRING, ResourceLocation::toString);
	}

	@Override
	protected Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftCraftingFirst = Comparator.comparing((String s) -> {
			String vanillaCrafting = VanillaRecipeCategoryUid.CRAFTING.toString();
			return s.equals(vanillaCrafting);
		}).reversed();
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.startsWith(ModIds.MINECRAFT_ID)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftCraftingFirst.thenComparing(minecraftFirst).thenComparing(naturalOrder);
	}

}

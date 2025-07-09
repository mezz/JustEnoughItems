package mezz.jei.common;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.resources.ResourceLocation;

public final class Constants {
	public static final IRecipeType<?> UNIVERSAL_RECIPE_TRANSFER_TYPE = IRecipeType.create(ModIds.JEI_ID, "universal_recipe_transfer_handler", Object.class);
	public static final ResourceLocation LOCATION_JEI_GUI_TEXTURE_ATLAS = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "textures/atlas/gui.png");
	public static final ResourceLocation JEI_GUI_TEXTURE_ATLAS_ID = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "gui");

	private Constants() {

	}
}

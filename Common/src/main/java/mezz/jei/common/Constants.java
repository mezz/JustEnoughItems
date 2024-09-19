package mezz.jei.common;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.ModIds;

public final class Constants {
	public static final RecipeType<?> UNIVERSAL_RECIPE_TRANSFER_TYPE = RecipeType.create(ModIds.JEI_ID, "universal_recipe_transfer_handler", Object.class);
	public static final ResourceLocation LOCATION_JEI_GUI_TEXTURE_ATLAS = new ResourceLocation(ModIds.JEI_ID, "textures/atlas/gui.png");
	public static final ResourceLocation NETWORK_CHANNEL_ID = new ResourceLocation(ModIds.JEI_ID, "channel");

	private Constants() {

	}
}

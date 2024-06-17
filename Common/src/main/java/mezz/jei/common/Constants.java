package mezz.jei.common;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

public final class Constants {
	public static final String TEXTURE_GUI_PATH = "textures/jei/gui/";
	public static final String TEXTURE_GUI_VANILLA = Constants.TEXTURE_GUI_PATH + "gui_vanilla.png";

	public static final ResourceLocation RECIPE_GUI_VANILLA = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, TEXTURE_GUI_VANILLA);

	public static final RecipeType<?> UNIVERSAL_RECIPE_TRANSFER_TYPE = RecipeType.create(ModIds.JEI_ID, "universal_recipe_transfer_handler", Object.class);
	public static final ResourceLocation LOCATION_JEI_GUI_TEXTURE_ATLAS = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "textures/atlas/gui.png");

	/**
	 * Ingredients with this tag will be hidden from JEI.
	 */
	public static final ResourceLocation HIDDEN_INGREDIENT_TAG = ResourceLocation.fromNamespaceAndPath("c", "hidden_from_recipe_viewers");

	private Constants() {

	}
}

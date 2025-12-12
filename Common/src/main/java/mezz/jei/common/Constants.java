package mezz.jei.common;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;

public final class Constants {
	public static final IRecipeType<?> UNIVERSAL_RECIPE_TRANSFER_TYPE = IRecipeType.create(ModIds.JEI_ID, "universal_recipe_transfer_handler", Object.class);
	public static final Identifier LOCATION_JEI_GUI_TEXTURE_ATLAS = Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "textures/atlas/gui.png");
	public static final Identifier JEI_GUI_TEXTURE_ATLAS_ID = Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "gui");

	private Constants() {

	}
}

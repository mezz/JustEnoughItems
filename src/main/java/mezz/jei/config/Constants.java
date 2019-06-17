package mezz.jei.config;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.ModIds;

public final class Constants {
	public static final String TEXTURE_GUI_PATH = "textures/gui/";
	public static final String TEXTURE_GUI_VANILLA = Constants.TEXTURE_GUI_PATH + "gui_vanilla.png";

	public static final ResourceLocation RECIPE_GUI_VANILLA = new ResourceLocation(ModIds.JEI_ID, TEXTURE_GUI_VANILLA);

	public static final int MAX_TOOLTIP_WIDTH = 150;

	public static final ResourceLocation UNIVERSAL_RECIPE_TRANSFER_UID = new ResourceLocation(ModIds.JEI_ID, "universal_recipe_transfer_handler");
	public static final ResourceLocation LOCATION_JEI_GUI_TEXTURE_ATLAS = new ResourceLocation(ModIds.JEI_ID, "textures/atlas/gui.png");

	private Constants() {

	}
}

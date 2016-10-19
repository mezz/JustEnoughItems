package mezz.jei.config;

import java.util.Locale;

import mezz.jei.util.Translator;

public class Constants {
	public static final String minecraftModName = "Minecraft";

	// Mod info
	public static final String MOD_ID = "jei";
	public static final String NAME = "Just Enough Items";
	public static final String VERSION = "@VERSION@";

	// Textures
	public static final String RESOURCE_DOMAIN = MOD_ID.toLowerCase(Locale.ENGLISH);
	public static final String TEXTURE_GUI_PATH = "textures/gui/";
	public static final String TEXTURE_RECIPE_BACKGROUND_PATH = Constants.TEXTURE_GUI_PATH + "recipeBackground.png";
	public static final String TEXTURE_RECIPE_BACKGROUND_TALL_PATH = Constants.TEXTURE_GUI_PATH + "recipeBackgroundTall.png";

	public static final int MAX_TOOLTIP_WIDTH = 125;

	public static final String RECIPE_TRANSFER_TOOLTIP = Translator.translateToLocal("jei.tooltip.transfer");

	public static final String UNIVERSAL_RECIPE_TRANSFER_UID = "universal recipe transfer handler";

	private Constants() {

	}
}

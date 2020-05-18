package mezz.jei.config;

import java.util.Locale;

import net.minecraft.util.ResourceLocation;

public final class Constants {
	public static final String MINECRAFT_NAME = "Minecraft";

	// Mod info
	public static final String MOD_ID = "jei";
	public static final String NAME = "Just Enough Items";
	public static final String VERSION = "@VERSION@";

	// Textures
	public static final String RESOURCE_DOMAIN = MOD_ID.toLowerCase(Locale.ENGLISH);
	public static final String TEXTURE_GUI_PATH = "textures/gui/";
	public static final String TEXTURE_GUI_VANILLA = Constants.TEXTURE_GUI_PATH + "gui_vanilla.png";

	public static final ResourceLocation RECIPE_GUI_VANILLA = new ResourceLocation(RESOURCE_DOMAIN, TEXTURE_GUI_VANILLA);

	public static final int MAX_TOOLTIP_WIDTH = 150;

	public static final String UNIVERSAL_RECIPE_TRANSFER_UID = "universal recipe transfer handler";

	private Constants() {

	}
}

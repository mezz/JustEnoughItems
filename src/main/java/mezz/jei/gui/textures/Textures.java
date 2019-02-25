package mezz.jei.gui.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import mezz.jei.config.Constants;

public class Textures {
	private final JeiTextureMap textureMap;

	public final TextureInfo slot;
	public final TextureInfo tabSelected;
	public final TextureInfo tabUnselected;
	public final TextureInfo buttonDisabled;
	public final TextureInfo buttonEnabled;
	public final TextureInfo buttonHighlight;
	public final TextureInfo guiBackground;
	public final TextureInfo recipeBackground;
	public final TextureInfo searchBackground;

	public final TextureInfo shapelessIcon;
	public final TextureInfo arrowPrevious;
	public final TextureInfo arrowNext;
	public final TextureInfo recipeTransfer;
	public final TextureInfo configButtonIcon;
	public final TextureInfo configButtonCheatIcon;
	public final TextureInfo bookmarkButtonDisabledIcon;
	public final TextureInfo bookmarkButtonEnabledIcon;
	public final TextureInfo infoIcon;
	public final TextureInfo catalystTab;
	public final TextureInfo flameIcon;

	public Textures(JeiTextureMap textureMap) {
		this.textureMap = textureMap;

		this.slot = registerGuiSprite("slot", 18, 18)
			.slice(4, 4, 4, 4);
		this.tabSelected = registerGuiSprite("tab_selected", 24, 24);
		this.tabUnselected = registerGuiSprite("tab_unselected", 24, 24);
		this.buttonDisabled = registerGuiSprite("button_disabled", 20, 20)
			.slice(2, 2, 2, 2);
		this.buttonEnabled = registerGuiSprite("button_enabled", 20, 20)
			.slice(2, 2, 2, 2);
		this.buttonHighlight = registerGuiSprite("button_highlight", 20, 20)
			.slice(2, 2, 2, 2);
		this.guiBackground = registerGuiSprite("gui_background", 64, 64)
			.slice(16, 16, 16, 16);
		this.recipeBackground = registerGuiSprite("single_recipe_background", 64, 64)
			.slice(16, 16, 16, 16);
		this.searchBackground = registerGuiSprite("search_background", 20, 20)
			.slice(4, 4, 4, 4);
		this.catalystTab = registerGuiSprite("catalyst_tab", 28, 28)
			.slice(8, 9, 8, 8);

		this.shapelessIcon = registerGuiSprite("icons/shapeless_icon", 36, 36)
			.trim(1, 2, 1, 1);
		this.arrowPrevious = registerGuiSprite("icons/arrow_previous", 9, 9)
			.trim(0, 0, 1, 1);
		this.arrowNext = registerGuiSprite("icons/arrow_next", 9, 9)
			.trim(0, 0, 1, 1);
		this.recipeTransfer = registerGuiSprite("icons/recipe_transfer", 7, 7);
		this.configButtonIcon = registerGuiSprite("icons/config_button", 16, 16);
		this.configButtonCheatIcon = registerGuiSprite("icons/config_button_cheat", 16, 16);
		this.bookmarkButtonDisabledIcon = registerGuiSprite("icons/bookmark_button_disabled", 16, 16);
		this.bookmarkButtonEnabledIcon = registerGuiSprite("icons/bookmark_button_enabled", 16, 16);
		this.infoIcon = registerGuiSprite("icons/info", 16, 16);
		this.flameIcon = registerGuiSprite("icons/flame", 14, 14);
	}

	private TextureInfo registerGuiSprite(String name, int width, int height) {
		TextureAtlasSprite textureAtlasSprite = textureMap.registerSprite(new ResourceLocation(Constants.MOD_ID, "gui/" + name));
		ResourceLocation location = textureMap.getLocation();
		return new TextureInfo(location, textureAtlasSprite, width, height);
	}
}

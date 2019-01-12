package mezz.jei.gui.textures;

import mezz.jei.config.Constants;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class Textures {
	private final JeiTextureMap textureMap;

	public final TextureAtlasSprite slot;
	public final TextureAtlasSprite tabSelected;
	public final TextureAtlasSprite tabUnselected;
	public final TextureAtlasSprite buttonDisabled;
	public final TextureAtlasSprite buttonEnabled;
	public final TextureAtlasSprite buttonHighlight;
	public final TextureAtlasSprite guiBackground;
	public final TextureAtlasSprite recipeBackground;
	public final TextureAtlasSprite searchBackground;

	public final TextureAtlasSprite shapelessIcon;
	public final TextureAtlasSprite arrowPrevious;
	public final TextureAtlasSprite arrowNext;
	public final TextureAtlasSprite recipeTransfer;
	public final TextureAtlasSprite configButtonIcon;
	public final TextureAtlasSprite configButtonCheatIcon;
	public final TextureAtlasSprite bookmarkButtonDisabledIcon;
	public final TextureAtlasSprite bookmarkButtonEnabledIcon;
	public final TextureAtlasSprite infoIcon;
	public final TextureAtlasSprite catalystTab;
	public final TextureAtlasSprite flameIcon;

	public Textures(JeiTextureMap textureMap) {
		this.textureMap = textureMap;

		this.slot = registerGuiSprite("slot");
		this.tabSelected = registerGuiSprite("tab_selected");
		this.tabUnselected = registerGuiSprite("tab_unselected");
		this.buttonDisabled = registerGuiSprite("button_disabled");
		this.buttonEnabled = registerGuiSprite("button_enabled");
		this.buttonHighlight = registerGuiSprite("button_highlight");
		this.guiBackground = registerGuiSprite("gui_background");
		this.recipeBackground = registerGuiSprite("recipe_background");
		this.searchBackground = registerGuiSprite("search_background");
		this.catalystTab = registerGuiSprite("catalyst_tab");

		this.shapelessIcon = registerGuiSprite("icons/shapeless_icon");
		this.arrowPrevious = registerGuiSprite("icons/arrow_previous");
		this.arrowNext = registerGuiSprite("icons/arrow_next");
		this.recipeTransfer = registerGuiSprite("icons/recipe_transfer");
		this.configButtonIcon = registerGuiSprite("icons/config_button");
		this.configButtonCheatIcon = registerGuiSprite("icons/config_button_cheat");
		this.bookmarkButtonDisabledIcon = registerGuiSprite("icons/bookmark_button_disabled");
		this.bookmarkButtonEnabledIcon = registerGuiSprite("icons/bookmark_button_enabled");
		this.infoIcon = registerGuiSprite("icons/info");
		this.flameIcon = registerGuiSprite("icons/flame");
	}

	private TextureAtlasSprite registerGuiSprite(String name) {
		return textureMap.registerSprite(new ResourceLocation(Constants.MOD_ID, "gui/" + name));
	}

	public JeiTextureMap getTextureMap() {
		return textureMap;
	}
}

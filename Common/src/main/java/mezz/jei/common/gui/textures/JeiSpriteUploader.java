package mezz.jei.common.gui.textures;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Constants;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;

public class JeiSpriteUploader extends TextureAtlasHolder {
	public JeiSpriteUploader(TextureManager textureManager) {
		super(textureManager, Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS, new ResourceLocation(ModIds.JEI_ID, "gui"));
	}

	/**
	 * Overridden to make it public
	 */
	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return super.getSprite(location);
	}

}

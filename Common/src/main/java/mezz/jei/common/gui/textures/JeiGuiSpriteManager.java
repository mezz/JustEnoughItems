package mezz.jei.common.gui.textures;

import mezz.jei.common.Constants;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;

import java.util.Set;

public class JeiGuiSpriteManager extends TextureAtlasHolder {
	public JeiGuiSpriteManager(TextureManager textureManager) {
		super(textureManager, Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS, Constants.JEI_GUI_TEXTURE_ATLAS_ID, Set.of(AnimationMetadataSection.TYPE, GuiMetadataSection.TYPE));
	}

	/**
	 * Overridden to make it public
	 */
	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return super.getSprite(location);
	}

	public GuiSpriteScaling getSpriteScaling(TextureAtlasSprite sprite) {
		return this.getMetadata(sprite).scaling();
	}

	private GuiMetadataSection getMetadata(TextureAtlasSprite sprite) {
		SpriteContents contents = sprite.contents();
		ResourceMetadata metadata = contents.metadata();
		return metadata.getSection(GuiMetadataSection.TYPE)
			.orElse(GuiMetadataSection.DEFAULT);
	}

}

package mezz.jei.gui.textures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.client.renderer.texture.SpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import mezz.jei.config.Constants;

public class JeiSpriteUploader extends SpriteUploader {
	private final Set<ResourceLocation> registeredSprites = new HashSet<>();

	public JeiSpriteUploader(TextureManager p_i50905_1_) {
		super(p_i50905_1_, Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS, "gui");
	}

	public void registerSprite(ResourceLocation location) {
		registeredSprites.add(location);
	}

	@Override
	protected Stream<ResourceLocation> getResourceLocations() {
		return Collections.unmodifiableSet(registeredSprites).stream();
	}

	/**
	 * Overridden to make it public
	 */
	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return super.getSprite(location);
	}

}

package mezz.jei.common.gui.textures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import mezz.jei.common.Constants;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class JeiSpriteUploader extends TextureAtlasHolder {
	private final Set<ResourceLocation> registeredSprites = new HashSet<>();

	public JeiSpriteUploader(TextureManager textureManager) {
		super(textureManager, Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS, "gui");
	}

	public void registerSprite(ResourceLocation location) {
		registeredSprites.add(location);
	}

	@Override
	protected Stream<ResourceLocation> getResourcesToLoad() {
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
